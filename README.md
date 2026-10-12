# Ramadeepthi Badireddy — Portfolio & Private Analytics

A responsive portfolio preserving the original visual layout, with a restrained monochrome Chrome-inspired palette, Avenir Next-first typography, a custom `R.` brand mark, real MySQL-backed visitor analytics, and a protected admin dashboard.

## Stack

- React + Vite
- Spring Boot 3.4 + Java 21
- MySQL
- Spring Security HTTP Basic for admin API access
- Recharts for dashboard visualizations

## Run locally

Prerequisites: Node.js 20+, Java 21, Maven, and MySQL.

1. Create a MySQL database named `portfolio` if it does not exist.
2. The startup script creates `backend/.env` automatically on first run. If your MySQL account uses a password, edit `DATABASE_PASSWORD` in that file before starting. The default local admin email and password are listed below; change the password before deployment.
3. From the project root run:

```bash
npm install
./start-local.sh
```

The script checks MySQL and configuration, starts the Spring Boot API, waits for its health endpoint, then starts Vite. It does not kill other processes to free ports. If a port is occupied, stop the process you recognize or change the port in your configuration.

- Portfolio: http://localhost:5173
- Admin dashboard: http://localhost:5173/admin
- API health: http://localhost:8080/api/health

**If the contact form says it cannot reach the API:** keep the terminal running after `./start-local.sh` starts both services. Open `http://localhost:8080/api/health` in a new tab; it must return `{"status":"ok"}`. If it does not, the backend is not ready (check the backend terminal for MySQL credentials, database, or port errors). If health works but the form fails, check the browser Console for a CORS error and ensure you opened the exact Vite Local URL printed by the startup script. The form cannot save enquiries when the API is stopped.

## Genuine analytics only

No sample, random, or localStorage-backed analytics are generated. Events are sent to the Spring Boot API and persisted in MySQL. When there are no real events, dashboard charts display empty states.

Tracked events include page views, estimated active time, and link clicks. Anonymous visitor IDs use browser local storage; session IDs use session storage. Development traffic is tagged and excluded from admin analytics by default. Time-on-page is an estimate: browser lifecycle events, blockers, and closed tabs can affect accuracy.

## API

- `POST /api/events` — record a real event (public)
- `POST /api/contact` — validate and save a contact enquiry (public)
- `GET /api/health` — backend/database readiness (public)
- `GET /api/admin/analytics?days=7` — event data (admin authentication required)
- `GET /api/admin/contacts?days=7` — enquiries (admin authentication required)

The admin sign-in form uses the credentials configured only in the backend environment. It keeps a Basic authorization value in the current browser tab's session storage and clears it on logout; deploy behind HTTPS and keep admin credentials private. Do not commit `backend/.env`.

## Deployment notes

Deploy the Vite frontend and Spring Boot API separately, configure `VITE_API_BASE_URL` to the deployed API origin, and provide the backend environment variables through the hosting provider's secret configuration. Use managed MySQL with backups, HTTPS, and an appropriate CORS origin. Run the SQL schema from `backend/src/main/resources/schema.sql` during deployment or let Spring's configured schema initializer apply it. Do not expose admin endpoints publicly without authentication.

## Projects

- Multi-Tenant Workforce Management — https://github.com/Ramadeepthi755/Multi-Tenant_SaaS_Platform
- YatraSetu — https://github.com/Ramadeepthi755/Yatrasetu (live demo: https://yatrasetu-cyan.vercel.app)
- Employee Management System — https://github.com/Ramadeepthi755/EmployeeManagementSystem
- YouTube Clone — https://github.com/Ramadeepthi755/Youtube_clone


## Local admin dashboard credentials

- Dashboard URL: `http://localhost:5173/admin`
- Email: `ramadeepthibadireddy@gmail.com`
- Password: use the value configured in your local `backend/.env` file. Never commit this file or share your password.

These are local-development defaults only. Change `ADMIN_PASSWORD` in `backend/.env` before exposing the site publicly. The database is MySQL (`portfolio`); the startup script creates the database if the configured MySQL account has permission.


## Verify real analytics persistence

1. Start MySQL and run `./start-local.sh`.
2. Open the portfolio (not `/admin`), click a few links, wait 5–10 seconds, then switch tabs or close the page.
3. Submit the contact form with a test message.
4. In MySQL, run:

```sql
USE portfolio;
SELECT event_type, page_path, COUNT(*) AS total
FROM portfolio_events GROUP BY event_type, page_path ORDER BY total DESC;
SELECT id, name, email, subject, created_at FROM portfolio_contacts ORDER BY id DESC LIMIT 10;
```

5. Sign in at `http://localhost:5173/admin`. Localhost traffic is intentionally marked as development and excluded from the normal analytics totals. Use `/api/admin/diagnostics` (with admin Basic auth) to confirm total rows were persisted, including development events. Public deployment traffic is counted in the main analytics dashboard.

The tracker uses `sendBeacon` with a CORS-safelisted `text/plain` payload for page-hide duration events, and sends the correct page path when hash navigation changes. The private `/admin` dashboard is excluded from public visitor metrics.


## Admin login troubleshooting

The admin sign-in form validates credentials through `POST /api/auth/login` first. If credentials are accepted but the dashboard cannot load, the error is a database/API issue rather than an incorrect password. Confirm `backend/.env` contains `ADMIN_EMAIL`, `ADMIN_PASSWORD` (at least 12 characters), `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`, then stop and restart `./start-local.sh`. Never commit `backend/.env`; use a unique password for deployments.


## Contact form verification

The contact form submits to `POST /api/contact`. A successful save returns HTTP `201 Created` and a JSON status. The UI shows success only after a successful HTTP response. On failure, it now displays the API's error message when available; a network error suggests checking that Spring Boot is running and `VITE_API_BASE_URL` matches the backend URL. Do not submit real personal information while testing.


## Admin analytics dashboard

The private `/admin` dashboard uses the Spring Boot API and MySQL records; it does not seed or display invented traffic. The overview now includes:
- KPI cards for page views, unique visitors, contact enquiries, and average tracked time
- a time-series traffic chart using recorded page-view events and saved contact records
- top pages with views, visitor counts, and available duration data
- traffic sources derived from stored referrer values
- recent visitors and recent events based on real analytics events
- responsive layout with a monochrome charcoal/black/white visual system

Device breakdown is read from the `device_category` metadata captured by the frontend. Events recorded before device metadata was added may display as `Unknown`. Traffic source reporting depends on the browser sending a referrer; direct visits are labelled `Direct`.
