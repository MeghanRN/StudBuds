package com.studbuds.payload;
// ⚠️ we could use @Email, @Pattern, @Valid, @Data, @NotBlank, to possibly speed up coding.
import java.util.HashSet;
import java.util.Set;

// SignupRequest class with added name field
public class SignupRequest {
   private String name;
   private String email;
   private String password;
   private String major;
   private int year;
   
   // Simulating a database of registered emails
   public static Set<String> registeredEmails = new HashSet<>();
   
   // Getters and setters
   public String getName() { return name; }
   public void setName(String name) { this.name = name; }
   
   public String getEmail() { return email; }
   public void setEmail(String email) {
       if (email.endsWith("@cooper.edu")) {
           this.email = email;
           registeredEmails.add(email); // Register email upon sign-up
       } else {
           throw new IllegalArgumentException("Email must be a @cooper.edu address");
       }
   }
   
   public String getPassword() { return password; }
   public void setPassword(String password) {
       if (password.length() >= 9) {
           this.password = password;
       } else {
           throw new IllegalArgumentException("Password must be at least 9 characters long");
       }
   }
   
   public String getMajor() { return major; }
   public void setMajor(String major) { this.major = major; }
   public int getYear() { return year; }
   public void setYear(int year) { this.year = year; }
}
