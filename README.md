# Auth Service

A Spring Boot authentication service supporting JWT-based authentication, OAuth2 social login (Google & GitHub), email verification, and MySQL persistence.

---

## Features

- JWT access & refresh token authentication
- OAuth2 social login — Google and GitHub
- Email support via SMTP (e.g. password reset, verification)
- MySQL database with HikariCP connection pooling
- Spring Security with fine-grained logging
- Environment-variable-driven configuration (no secrets in code)

---

## Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Language     | Java                              |
| Framework    | Spring Boot                       |
| Security     | Spring Security, JWT, OAuth2      |
| Database     | MySQL                             |
| ORM          | Spring Data JPA / Hibernate       |
| Connection   | HikariCP                          |
| Mail         | Spring Mail (SMTP / STARTTLS)     |
| Build Tool   | Maven / Gradle                    |

---

## Prerequisites

- Java 17+
- MySQL 8+
- Maven or Gradle
- A Google OAuth2 app (console.cloud.google.com)
- A GitHub OAuth2 app (github.com/settings/developers)
- An SMTP email account (Gmail App Password, SendGrid, etc.)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/auth-service.git
cd auth-service
```

### 2. Set up the database

```sql
CREATE DATABASE DB_NAME;
```

### 3. Configure environment variables

Create a `.env` file or set the following in your system/IDE:

```env
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# GitHub OAuth2
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# Database
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password

# Mail
MAIL_USERNAME=your-email@example.com
MAIL_APP_PASSWORD=your-app-password

# JWT
JWT_SECRET=your-very-long-secret-key
JWT_ISSUER=your-app-name
JWT_REF_COOKIE_NAME=refresh_token
JWT_COOKIE_SECURE=true
JWT_COOKIE_HTTP_ONLY=true
JWT_COOKIE_SAME_SITE=lax
JWT_COOKIE_DOMAIN=localhost
```

### 4. Run the application

```bash
# Maven
./mvnw spring-boot:run

# Gradle
./gradlew bootRun
```

---

## Configuration Overview

All configuration lives in `src/main/resources/application.yml`.

| Setting | Description |
|---|---|
| `security.jwt.access-token-expiration` | Access token TTL in seconds (default: 900) |
| `security.jwt.refresh-token-expiration` | Refresh token TTL in seconds (default: 604800 / 7 days) |
| `spring.jpa.hibernate.ddl-auto` | Set to `validate` or `none` in production |
| `spring.datasource.hikari.*` | Connection pool settings |
| `spring.mail.*` | SMTP configuration |

---

## OAuth2 Redirect URIs

Register these in your OAuth2 provider dashboards:

**Google:**
```
http://localhost:PORT/login/oauth2/code/google
```

**GitHub:**
```
http://localhost:PORT/login/oauth2/code/github
```

---

## Frontend Integration

After a successful auth flow, the user is redirected to:

- **Success:** `http://localhost:5173/dashboard`
- **Failure:** `http://localhost:5173/register`

Update these under `app.auth.frontend` in `application.yml` for production.

---

## Security Notes

- Never commit `application.yml` with real credentials — use environment variables
- Set `spring.jpa.hibernate.ddl-auto: validate` in production
- Set `security.jwt.cookie-secure: true` in production (requires HTTPS)
- Rotate your JWT secret periodically

---

## Project Structure

```
src/
└── main/
    ├── java/com/auth/Auth/
    │   ├── config/        # Security, JWT, OAuth2 configuration
    │   ├── controller/    # Auth endpoints
    │   ├── service/       # Business logic
    │   ├── repository/    # JPA repositories
    │   ├── model/         # Entity classes
    │   └── dto/           # Request / Response DTOs
    └── resources/
        └── application.yml
```

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
