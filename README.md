# 🚀 Full-Stack Developer Portfolio & Analytics Platform

A modern, responsive personal developer portfolio featuring an interactive project showcase, contact messenger, and a real-time event analytics dashboard backend.

🔗 **Live Portfolio:** [portfolio-production-9361.up.railway.app](https://portfolio-production-9361.up.railway.app/)

---

## 🛠️ Tech Stack

### Frontend
- **Framework:** React 18 / Vite
- **Styling:** CSS3 (Custom responsive layout & CSS variables)
- **Icons:** Lucide React
- **Data Visualization:** Recharts
- **Hosting:** Railway (Static SPA)

### Backend
- **Framework:** Spring Boot 3.4.x (Java 21)
- **Security:** Spring Security (JWT / Role-based authorization, Custom CORS filter)
- **Persistence:** Spring Data JPA / HikariCP
- **Database:** MySQL
- **Build Tool:** Maven
- **Hosting:** Railway (Containerized Service)

---

## ✨ Features

- **Responsive Portfolio UI:** Clean dark-mode interface highlighting skills, featured projects, and developer background.
- **Direct Messaging Pipeline:** Contact form submitting directly to a REST API with server-side validation.
- **Analytics & Tracking Backend:** Collects page hits, event metrics, and interaction telemetry.
- **Secure Architecture:** Decoupled frontend/backend setup with CSRF protection tuned for external REST clients and granular CORS origin control.

---

## ⚙️ Environment Variables

### Frontend (.env / Railway Variables)

| Variable | Description | Example |
| :--- | :--- | :--- |
| VITE_API_BASE_URL | Base URL of the deployed Spring Boot backend | https://pleasing-vision-production-a984.up.railway.app |

### Backend (application.properties / Railway Variables)

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| PORT | Dynamic web port assigned by host | 8080 |
| DATABASE_URL | MySQL JDBC connection string | jdbc:mysql://<HOST>:<PORT>/<DATABASE> |
| DATABASE_USERNAME | Database username | root |
| DATABASE_PASSWORD | Database password | ****** |
| FRONTEND_ORIGIN | Allowed origin domain for CORS | https://portfolio-production-9361.up.railway.app |

---

## 🚀 Local Development Setup

### Prerequisites
- Node.js (v20+) & npm
- JDK 21+
- Maven 3.9+
- MySQL Server running locally

---

### 1. Backend Setup

1. Navigate to the backend directory:
   cd backend

2. Configure your MySQL credentials in src/main/resources/application.properties:
   spring.datasource.url=jdbc:mysql://localhost:3306/portfolio?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_password

3. Build and run the service:
   mvn clean spring-boot:run

4. Verify backend health:
   curl http://localhost:8080/api/health
   # Expected response: {"status":"ok"}

---

### 2. Frontend Setup

1. From the project root, install dependencies:
   npm install

2. Create a local .env file in the project root:
   VITE_API_BASE_URL=http://localhost:8080

3. Start the Vite development server:
   npm run dev

4. Open http://localhost:5173 in your browser.

---

## 📦 Deployment (Railway)

1. **Database:** Deploy a managed MySQL database instance.
2. **Backend:** Deploy the backend directory as a container service. Link the MySQL environment variables to the DATABASE_* configurations.
3. **Frontend:** Deploy the root React app. Set VITE_API_BASE_URL to your live backend domain and trigger a build so Vite embeds the production API endpoint.

---

## 📬 Contact

- **Name:** Badireddy Ramadeepthi
- **Email:** [ramadeepthibadireddy@gmail.com](mailto:ramadeepthibadireddy@gmail.com)
- **Portfolio:** [portfolio-production-9361.up.railway.app](https://portfolio-production-9361.up.railway.app/)
