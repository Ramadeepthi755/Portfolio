# Implementation checklist

- [x] Preserve the original React portfolio layout and existing project content.
- [x] Apply a monochrome palette and Avenir Next-first font stack.
- [x] Keep the `R.` navbar brand mark.
- [x] Remove Supabase dependency and replace frontend persistence with Spring Boot API calls.
- [x] Record page views, link clicks, estimated time-on-page, anonymous visitor ID, session ID, referrer, device metadata, and dev/prod flag.
- [x] Do not fabricate analytics; empty states appear when no records exist.
- [x] Persist visitor events and contact enquiries in MySQL.
- [x] Protect admin analytics and contact endpoints with Spring Security HTTP Basic authentication.
- [x] Add traffic chart, page-distribution pie chart, device-distribution pie chart, page analytics, and contact enquiry views.
- [x] Add environment template, `.gitignore`, local startup script, health endpoint, and setup documentation.
- [ ] Run frontend production build — BLOCKED in this environment because npm registry packages are unavailable and Vite is not installed here.
- [ ] Run Maven compilation/tests — BLOCKED in this environment because Maven is not installed here.
- [ ] Verify with the user's local MySQL credentials — BLOCKED because this environment does not have access to the user's Mac database or secrets.


## Analytics filter note
- The admin analytics endpoint excludes events flagged as local development traffic by default.
- When the admin dashboard itself is opened on `localhost` or `127.0.0.1`, it requests `includeDev=true` so local testing events can be inspected. Production dashboards continue excluding those development events by default.


## Analytics tracking note
- Active time is recorded periodically while the portfolio tab is visible. Time spent with the tab hidden is excluded.
- Device category is captured from browser metadata on each event.
- Browser-based analytics can be affected by privacy settings, blockers, or network failures; verify using real visits.
