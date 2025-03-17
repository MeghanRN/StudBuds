package com.studbuds.payload;


// DeleteAccountRequest class for user account deletion
public class DeleteAccountRequest {
   private String email;
   private String password;
   
   // Getters and setters
   public String getEmail() { return email; }
   public void setEmail(String email) {
       if (email.endsWith("@cooper.edu")) { 
           this.email = email;
       } else {
           throw new IllegalArgumentException("Email must be a @cooper.edu address"); // ⚠️ we could use @Email and @pattern to enforce the domain!
       }
   }
   
   public String getPassword() { return password; }
   public void setPassword(String password) {
       if (password.length() >= 9) { // ⚠️ we could use @Size here too as this setter throws an exception, which isn't the best way to handle validation.
           this.password = password;
       } else {
           throw new IllegalArgumentException("Password must be at least 9 characters long");
       }
   }
}
