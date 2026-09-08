## What this is
A Spring Boot Java application that implements an authentication service (controllers, OTP flow, user management) — intended as the auth component for a larger system (BlueScope). The code organizes REST controllers, service layer, persistence (repositories/entities) and security configuration for authentication/OTP flows.

### Stack
- **Language(s):** Java (100%)
- **Framework / runtime:** Spring Boot (Maven-built Spring Boot application)
- **Notable libraries / concepts (from code structure):** Spring Web (MVC controllers), Spring Security (security package/config), Spring Data JPA (repo/entity layers), Maven wrapper for build/run

## How it's organized
```
.gitattributes            repo attributes
.gitignore                ignored files
.mvn/                     Maven wrapper support
mvnw, mvnw.cmd            Maven wrapper scripts
pom.xml                   Maven build file (project dependencies / plugins)
src/
  main/
    java/
      com/auth/Auth/
        AuthApplication.java                Spring Boot application entrypoint
        AuthServiceOTPpathnotesImpoelemt.md  notes / OTP design doc (markdown)
        config/       configuration classes (security, app config)
        controller/   REST controllers (AuthController.java, OtpController.java, UserController.java)
        dto/          request/response DTOs
        entity/       JPA entities / domain models
        exception/    custom exceptions / handlers
        repo/         repository interfaces (persistence)
        security/     security configuration / filters
        services/     service interfaces and implementations (AuthServices, UserServices, Implementation/)
        utils/        assorted helpers/utilities
    resources/       Spring resources (application.properties / static files)
  test/              test sources (unit/integration tests)
```

How it fits together: AuthApplication boots the Spring context. Incoming HTTP requests hit controllers under controller/ (e.g., AuthController, OtpController, UserController). Controllers call service layer classes (services/) which contain business logic and coordinate persistence via repo/ interfaces (entities live in entity/). security/ and config/ contain the authentication/authorization setup (filters, config beans) applied across controllers. The OTP flow is documented in AuthServiceOTPpathnotesImpoelemt.md and implemented across controller/services/security.

## How to run it
Shortest path from a fresh clone:

- Build and run with the included Maven wrapper (Unix/macOS):
  - ./mvnw spring-boot:run
- Windows:
  - mvnw.cmd spring-boot:run
- Build a JAR and run:
  - ./mvnw clean package
  - java -jar target/*.jar
- Run tests:
  - ./mvnw test

Typical required environment variables (common for Spring Boot apps and indicated by the repo structure):
- SPRING_DATASOURCE_URL (jdbc URL for your DB)
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- Any JWT secret or security config properties (e.g., JWT_SECRET) if used
Check src/main/resources/application.properties (or application.yml) for exact keys.

## Try asking
- Can you provide the exact environment variables and example application.properties values needed to run the app (DB, JWT secret, port)?
- Do you want a root README.md that documents the OTP flow in AuthServiceOTPpathnotesImpoelemt.md and the available API endpoints (AuthController, OtpController, UserController)?
- Should I add a Dockerfile + docker-compose (Postgres) and a short GitHub Actions workflow that builds and runs tests on every push?
