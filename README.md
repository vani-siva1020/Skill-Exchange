# Skill Exchange Platform

A full-stack, real-time skill-matching **app** built with **Spring Boot** (Java backend + REST API),
**H2** in-memory database, and a mobile-app-style **HTML/CSS/JavaScript** frontend.

## Real-world problem it solves
People often want to learn a new skill (cooking, guitar, a language, coding) but can't afford
classes — while someone nearby already knows that skill and wants to learn something *you* know.
This platform lets people list what they can **teach** and what they want to **learn**, matches
them automatically, lets them **book a session**, and afterwards **rate & review** each other —
a simple barter-style skill economy, end to end.

## Features
- 🔐 **Login / Signup** — passwords are hashed with BCrypt, never stored in plain text
- 🤝 **Real-time skill matching** — mutual (two-way) matches are ranked highest
- 📅 **Session booking** — request a session with a teacher, teacher confirms/declines, either side can cancel or mark it completed
- ⭐ **Ratings & Reviews** — after a session is completed, both sides can leave a star rating + comment; profile shows the average
- 📱 **App-style UI** — bottom tab navigation (Members / Matches / Sessions / Profile), no plain scrolling webpage

## Tech Stack
- Java 17, Spring Boot 3.2.5 (Web, Data JPA, Validation)
- `spring-security-crypto` for BCrypt password hashing (no full Spring Security filter chain — a lightweight custom token service instead)
- H2 in-memory database
- HTML, CSS, vanilla JavaScript — served as Spring Boot static resources, no separate frontend server

## Project Structure
```
skill-exchange/
├── pom.xml
├── .gitignore
└── src/main/
    ├── java/com/skillexchange/
    │   ├── SkillExchangeApplication.java
    │   ├── model/          (User, SkillSession, Review, MatchResult)
    │   ├── dto/             (RegisterRequest, LoginRequest, AuthResponse, BookSessionRequest, UpdateSessionStatusRequest, ReviewRequest)
    │   ├── repository/     (UserRepository, SessionRepository, ReviewRepository)
    │   ├── service/         (AuthService, TokenService, MatchService, SessionService, ReviewService)
    │   └── controller/     (AuthController, UserController, SessionController, ReviewController)
    └── resources/
        ├── application.properties
        └── static/ (index.html, style.css, script.js)
```

## How to Run (IntelliJ IDEA)
1. Open IntelliJ → **File → Open** → select the `skill-exchange` folder (the one that directly contains `pom.xml`) — not its parent folder.
2. If IntelliJ doesn't auto-detect it, right-click `pom.xml` → **Add as Maven Project**, and wait for dependencies to download (needs internet).
3. Open `SkillExchangeApplication.java` → click the green ▶ next to `main()` → **Run**.
4. Open your browser at **http://localhost:8080**.
5. (Optional) H2 console at **http://localhost:8080/h2-console** — JDBC URL `jdbc:h2:mem:skillexchange`, user `sa`, blank password.

## How to Use
1. **Register** an account with the skills you can teach and want to learn.
2. Go to **Members** or **Matches**, and tap **📅 Book Session** on someone.
3. The teacher sees the request under **Sessions** and can **Confirm** or **Decline**.
4. Once a session happens, either side marks it **Completed**.
5. Completed sessions unlock **⭐ Leave Review** — ratings show up on the other person's **Profile**.

## REST API
| Method | Endpoint                    | Auth | Description                          |
|--------|-------------------------------|------|---------------------------------------|
| POST   | `/api/auth/register`          | —    | Create an account                     |
| POST   | `/api/auth/login`             | —    | Log in, returns a token               |
| POST   | `/api/auth/logout`            | ✅   | Invalidate the current token          |
| GET    | `/api/users`                  | —    | List all users                        |
| GET    | `/api/users/{id}`             | —    | Get one user                          |
| GET    | `/api/users/{id}/matches`     | —    | Find skill-exchange matches           |
| GET    | `/api/users/{id}/rating`      | —    | Average rating + review count         |
| DELETE | `/api/users/{id}`             | —    | Remove a user                         |
| POST   | `/api/sessions`               | ✅   | Book a session (caller = learner)     |
| GET    | `/api/sessions/user/{id}`     | —    | All sessions for a user               |
| PUT    | `/api/sessions/{id}/status`   | ✅   | Confirm / cancel / complete a session |
| POST   | `/api/reviews`                | ✅   | Leave a review on a completed session |
| GET    | `/api/reviews/user/{id}`      | —    | Reviews received by a user            |

✅ = send the login token in the `Authorization` header.

## Push to GitHub
Run these inside the `skill-exchange` folder (same level as `pom.xml`):
```bash
git init
git add .
git commit -m "Skill Exchange full-stack app"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo-name>.git
git push -u origin main
```
(Create the empty repo on GitHub first, without a README, so there's no merge conflict.)

## Ideas to extend
- WebSocket-based real-time chat between matched users
- Push/email notifications for booking requests
- Filter matches by location or availability
- Deploy the H2 database to PostgreSQL for production use
