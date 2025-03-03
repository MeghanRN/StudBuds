Stud Buds™ Backend

This is the backend for the Stud Buds™ Study Buddy Matching App. It is built using Spring Boot, connects to a PostgreSQL database, and is containerized using Docker and Docker Compose.

Prerequisites:
- Java 11 - Download from https://adoptium.net/ or your preferred provider.
- Maven - See https://maven.apache.org/install.html for installation instructions.
- Docker and Docker Compose - Install Docker Desktop from https://www.docker.com/products/docker-desktop.
- Postman - For testing API endpoints (download from https://www.postman.com/downloads/).

Project Directory Structure:  
studbuds/   
└── backend/    
    ├── pom.xml    
    ├── Dockerfile    
    └── src/    
        └── main/    
            ├── java/com/studbuds/   
            │   ├── StudBudsApplication.java    
            │   ├── config/     
            │   │   SecurityConfig.java    
            │   ├── controller/    
            │   │   AuthController.java    
            │   │   UserController.java     
            │   │   MatchingController.java     
            │   ├── model/    
            │   │   User.java  
            │   │   Preference.java  
            │   │   Match.java   
            │   ├── payload/  
            │   │   SignupRequest.java  
            │   │   LoginRequest.java  
            │   ├── repository/  
            │   │   UserRepository.java  
            │   │   PreferenceRepository.java  
            │   │   MatchRepository.java  
            │   └── service/  
            │       MatchingService.java  
            └── resources/  
                application.properties  

Note: The target/ folder is automatically generated by Maven during the build process and should not be included in version control.

Build and Run Instructions:

Step 1: Build the Application with Maven
1. Open a terminal and navigate to the backend folder (where pom.xml is located):
   cd /path/to/studbuds/backend
2. Clean the project:
   mvn clean
3. Compile and package the application:
   mvn package
   This will compile your source files and package them into a JAR file (e.g., studbuds-backend-0.0.1-SNAPSHOT.jar) located in the target/ folder.
   Verify the build output shows no errors and that the JAR file is created.

Step 2: Start the Application with Docker Compose
1. From the project root (where docker-compose.yml is located), run:
   docker-compose up --build
   This command builds your backend image and starts both the backend and PostgreSQL containers.
   You should see logs indicating that the backend application starts (e.g., "Started StudBudsApplication in ... seconds") and that PostgreSQL is initialized and ready.

2. Verify that containers are running by opening a new terminal and running:
   docker ps
   You should see two containers:
     - studbuds-backend-1 running on port 8080.
     - studbuds-postgres-1 running on port 5432.

Step 3: Test the Endpoints Using Postman

3.1 User Signup:
- Method: POST
- URL: http://localhost:8080/api/auth/signup
- Headers: Content-Type: application/json
- Body (raw JSON):
{
  "email": "student@example.com",
  "password": "securePassword",
  "major": "Computer Science",
  "year": "2022
}
Action: Click Send and verify you receive a response confirming that the user was registered.

3.2 User Login:
- Method: POST
- URL: http://localhost:8080/api/auth/login
- Headers: Content-Type: application/json
- Body (raw JSON):
{
  "email": "student@example.com",
  "password": "securePassword"
}
Action: Click Send and check for the login response.

3.3 Fetch User Details:
- Method: GET
- URL: http://localhost:8080/api/user/1
Action: Click Send to view the details of the user with ID 1.

3.4 Update User Preferences:
- Method: POST
- URL: http://localhost:8080/api/user/1/preference
- Headers: Content-Type: application/json
- Body (raw JSON):
{
  "availableDays": "Monday,Wednesday,Friday",
  "subjects_to_learn": "Mathematics,Physics",
  "subjects_to_teach": "Chemistry,Biology"
}
Action: Click Send and verify that the response confirms that preferences are updated.
Note: The updated endpoint ensures that a user can have only one preference record.

3.5 Test the Matching Endpoint:
- Method: GET
- URL: http://localhost:8080/api/matches/find/1
Action: Click Send and check that a list of matched users is returned. Each matching result will include an overview of the matched user's preferences along with the common available days, common subjects, and a match score.

Step 4: Verify Database Content Directly
1. Access the PostgreSQL container by running:
   docker exec -it studbuds-postgres-1 psql -U your_username -d studbuds
2. In the PostgreSQL shell:
   - List tables with: \dt
   - Query the users table with: SELECT * FROM users;
   - Query the preferences table with: SELECT * FROM preferences;
   - Exit the shell with: \q

Step 5: Clean Up
When finished testing, stop the containers by running:
   docker-compose down
If you need a full reset (including database data), run:
   docker-compose down -v

Troubleshooting:
Invalid or Corrupt JAR File:
If you see an error like "Invalid or corrupt jarfile /app.jar", verify that:
  - Your JAR file in the target/ folder was built correctly using "mvn package".
  - Your Dockerfile uses the correct base image (openjdk:21-jdk-slim) since your project now targets Java 21.
Container Not Running:
If the backend container is not running (check with "docker ps"), inspect the logs with:
   docker-compose logs backend
Ensure that your docker-compose file has the correct build context and Dockerfile path.