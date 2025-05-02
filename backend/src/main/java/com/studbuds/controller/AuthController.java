package com.studbuds.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.studbuds.model.Preference;
import com.studbuds.model.User;
import com.studbuds.payload.SignupRequest;
import com.studbuds.repository.PreferenceRepository;
import com.studbuds.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private FirebaseAuth firebaseAuth;
    @Autowired private UserRepository userRepository;
    @Autowired private PreferenceRepository preferenceRepository;

    // ─── SIGNUP ───────────────────────────────────────────────────────────────
    /**
     * Client must:
     *   1) createUserWithEmailAndPassword(...)
     *   2) getIdToken() → Authorization: Bearer <token>
     *   3) POST /api/auth/signup { name, major, year }
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody SignupRequest req
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Missing or malformed Authorization header.");

        String idToken = authHeader.substring(7);
        FirebaseToken decoded;
        try {
            decoded = firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid Firebase token.");
        }

        String uid   = decoded.getUid();
        String email = decoded.getEmail();

        // avoid duplicate local record
        if (userRepository.findByFirebaseUid(uid).isPresent()) {
            return ResponseEntity.ok(Map.of("message","Local account already exists."));
        }

        // create local user
        User u = new User();
        u.setFirebaseUid(uid);
        u.setEmail(email);
        u.setName(req.getName());
        u.setMajor(req.getMajor());
        u.setYear(req.getYear());
        u.setCreatedAt(LocalDateTime.now());
        userRepository.save(u);

        // blank preference
        Preference p = new Preference();
        p.setUser(u);
        p.setAvailableDays("");
        p.setSubjectsToLearn("");
        p.setSubjectsToTeach("");
        preferenceRepository.save(p);

        u.setPreference(p);
        userRepository.save(u);

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(Map.of("message","Local account created. Please verify your email."));
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestHeader("Authorization") String authHeader
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Missing or malformed Authorization header.");

        String idToken = authHeader.substring(7);
        FirebaseToken decoded;
        try {
            decoded = firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid Firebase token.");
        }

        if (!decoded.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Please verify your email before logging in.");
        }

        String uid = decoded.getUid();
        return userRepository.findByFirebaseUid(uid)
            .map(u -> ResponseEntity.ok(Map.of(
                "message","Login successful.",
                "userId", u.getId()
            )))
            .orElseGet(() ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                              .body(Map.of("message", "No local account found. Please sign up first."))
            );
    }
}
