package com.studbuds.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.studbuds.model.User;
import com.studbuds.model.Preference;
import com.studbuds.model.Swipe;
import com.studbuds.model.Match;
import com.studbuds.payload.LoginRequest;
import com.studbuds.payload.SignupRequest;
import com.studbuds.payload.DeleteAccountRequest;
import com.studbuds.repository.UserRepository;
import com.studbuds.repository.MatchRepository;
import com.studbuds.repository.PreferenceRepository;
import com.studbuds.repository.SwipeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PreferenceRepository preferenceRepository;
    @Autowired private SwipeRepository swipeRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private FirebaseAuth firebaseAuth;

    // ─── SIGNUP ───────────────────────────────────────────────────────────────
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignupRequest req) {
        String email    = req.getEmail().trim().toLowerCase();
        String password = req.getPassword();
        String name     = req.getName();

        if (!email.endsWith("@cooper.edu"))
            return ResponseEntity.badRequest().body("Email must be a @cooper.edu address");
        if (password == null || password.length() < 9)
            return ResponseEntity.badRequest().body("Password must be at least 9 characters long");

        try {
            firebaseAuth.getUserByEmail(email);
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Email already in use. Please log in instead.");
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode() != com.google.firebase.auth.AuthErrorCode.USER_NOT_FOUND) {
                return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Firebase error: " + e.getMessage());
            }
        }

        try {
            UserRecord record = firebaseAuth.createUser(
                new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(name)
            );
            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "User created in Firebase. Verification email must be confirmed.");
            resp.put("uid", record.getUid());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (FirebaseAuthException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Firebase error: " + e.getMessage());
        }
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestBody(required = false) LoginRequest body,
        @RequestHeader(value = "Authorization", required = false) String header
    ) {
        String idToken = null;
        if (header != null && header.startsWith("Bearer ")) {
            idToken = header.substring(7);
        } else if (body != null && body.getFirebaseToken() != null) {
            idToken = body.getFirebaseToken();
        }

        if (idToken == null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Missing Firebase token");
        }

        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);
            if (!decoded.isEmailVerified()) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please verify your email before logging in.");
            }

            String uid = decoded.getUid();
            String email = decoded.getEmail();

            Optional<User> userOpt = userRepository.findByFirebaseUid(uid);
            User user;

            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                Optional<User> emailUserOpt = userRepository.findByEmailIgnoreCase(email);

                if (emailUserOpt.isPresent()) {
                    user = emailUserOpt.get();
                    user.setFirebaseUid(uid);
                    userRepository.save(user);
                } else {
                    UserRecord record = firebaseAuth.getUser(uid);

                    user = new User();
                    user.setName(record.getDisplayName() != null ? record.getDisplayName() : "Unnamed");
                    user.setEmail(record.getEmail());
                    user.setFirebaseUid(uid);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setMajor("Undeclared");
                    user.setYear("2025");
                    userRepository.save(user);

                    Preference pref = new Preference();
                    pref.setUser(user);
                    pref.setAvailableDays("");
                    pref.setSubjectsToLearn("");
                    pref.setSubjectsToTeach("");
                    preferenceRepository.save(pref);

                    user.setPreference(pref);
                    userRepository.save(user);
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Login successful.");
            resp.put("userId", user.getId());
            return ResponseEntity.ok(resp);

        } catch (FirebaseAuthException e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid or expired Firebase token.");
        }
    }

    // ─── DELETE ACCOUNT ──────────────────────────────────────────────────────
    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(
        @RequestBody(required = false) DeleteAccountRequest body,
        @RequestHeader(value = "Authorization", required = false) String header
    ) {
        String idToken = null;
        if (header != null && header.startsWith("Bearer ")) {
            idToken = header.substring(7);
        } else if (body != null) {
            idToken = body.getFirebaseToken();
        }
        if (idToken == null) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Missing Firebase token"));
        }

        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);
            String uid = decoded.getUid();

            Optional<User> userOpt = userRepository.findByFirebaseUid(uid);
            if (userOpt.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found."));
            }
            User user = userOpt.get();

            // 1) Delete all swipes sent or received by this user
            List<Swipe> out = swipeRepository.findByFromUser(user);
            List<Swipe> in  = swipeRepository.findByToUser(user);
            List<Swipe> allSwipes = new ArrayList<>();
            allSwipes.addAll(out);
            allSwipes.addAll(in);
            if (!allSwipes.isEmpty()) {
                swipeRepository.deleteAll(allSwipes);
            }

            // 2) Delete all matches involving this user
            List<Match> matches = matchRepository.findByUser1OrUser2(user, user);
            if (!matches.isEmpty()) {
                matchRepository.deleteAll(matches);
            }

            // 3) Delete the user's preferences
            preferenceRepository.findByUser(user)
                                .ifPresent(preferenceRepository::delete);

            // 4) Delete from Firebase Auth
            firebaseAuth.deleteUser(uid);

            // 5) Finally, delete the local user record
            userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "Account and all related data deleted successfully."));
        } catch (FirebaseAuthException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Firebase error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }
}