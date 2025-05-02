package com.studbuds.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.studbuds.model.*;
import com.studbuds.payload.*;
import com.studbuds.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private FirebaseAuth         firebaseAuth;
    @Autowired private UserRepository       userRepository;
    @Autowired private PreferenceRepository preferenceRepository;
    @Autowired private SwipeRepository      swipeRepository;
    @Autowired private MatchRepository      matchRepository;

    /*────────────────────────  SIGN‑UP  ────────────────────────*/
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(
            @RequestBody SignupRequest req,
            @RequestHeader("Authorization") String header) {

        String idToken = header.replace("Bearer ", "").trim();
        if (idToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing Firebase token");
        }

        FirebaseToken decoded;
        try {
            decoded = firebaseAuth.verifyIdToken(idToken, /*checkRevoked=*/true);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid Firebase token.");
        }

        /* 1) Basic validations on input */
        String email = req.getEmail().trim().toLowerCase();
        if (!email.endsWith("@cooper.edu"))
            return ResponseEntity.badRequest().body("Email must be @cooper.edu");
        if (!decoded.getEmail().equalsIgnoreCase(email))
            return ResponseEntity.badRequest().body("Token/email mismatch.");
        if (req.getPassword() == null || req.getPassword().length() < 9)
            return ResponseEntity.badRequest().body("Password must be ≥9 chars");

        /* 2) Duplicate‑check locally */
        if (userRepository.findByFirebaseUid(decoded.getUid()).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("Local account already exists.");

        /* 3) Persist local user with REAL fields */
        User u = new User();
        u.setFirebaseUid(decoded.getUid());
        u.setEmail(email);
        u.setName(req.getName().trim());
        u.setMajor(req.getMajor());
        u.setYear(req.getYear());
        u.setCreatedAt(LocalDateTime.now());
        userRepository.save(u);

        Preference p = new Preference();
        p.setUser(u);
        p.setAvailableDays("");
        p.setSubjectsToLearn("");
        p.setSubjectsToTeach("");
        preferenceRepository.save(p);

        u.setPreference(p);
        userRepository.save(u);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(Map.of("message",
                                 "Local account created. Please verify your email."));
    }

    /*────────────────────────  LOGIN  ─────────────────────────*/
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {

        String idToken = body.getFirebaseToken();
        if (idToken == null || idToken.isBlank())
            return ResponseEntity.badRequest().body("Missing Firebase token");

        FirebaseToken decoded;
        try {
            decoded = firebaseAuth.verifyIdToken(idToken, /*checkRevoked=*/true);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid or expired token.");
        }

        if (!decoded.isEmailVerified())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Please verify your email first.");

        return userRepository.findByFirebaseUid(decoded.getUid())
            .<ResponseEntity<?>>map(u -> ResponseEntity.ok(
                    Map.of("message", "Login successful.", "userId", u.getId())))
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                           .body("No local account. Please sign up."));
    }

    /*────────────────────────  DELETE ACCOUNT  ───────────────*/
    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestBody DeleteAccountRequest body) {

        String idToken = body.getFirebaseToken();
        if (idToken == null || idToken.isBlank())
            return ResponseEntity.badRequest().body("Missing Firebase token");

        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken, true);
            String uid = decoded.getUid();

            User user = userRepository.findByFirebaseUid(uid)
                          .orElseThrow(() -> new RuntimeException("User not found"));

            swipeRepository.deleteAll(swipeRepository.findByFromUser(user));
            swipeRepository.deleteAll(swipeRepository.findByToUser(user));
            matchRepository.deleteAll(matchRepository.findByUser1OrUser2(user, user));
            preferenceRepository.findByUser(user).ifPresent(preferenceRepository::delete);

            firebaseAuth.deleteUser(uid);
            userRepository.delete(user);

            return ResponseEntity.ok(Map.of("message", "Account fully deleted."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Delete failed: " + e.getMessage());
        }
    }
}
