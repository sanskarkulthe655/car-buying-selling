# AutoAdmin Motors — Public Showroom and Admin Plan

## Updated scope

Keep Home, Services and Contact Us; add separate public **New Cars** and **Second-Hand Cars** pages in the sidebar directly below Home. Public visitors can inspect vehicle details and choose **Buy**. Checkout is explicitly a demo: visitors can select Demo Card or Demo UPI and simulate success or failure; no real payment, card number, gateway or money movement is involved. A successful simulation records a payment attempt and sale in MySQL and marks the car SOLD. A failed attempt is logged but does not sell the car.

Add one protected Admin Dashboard. The user chose username/password login for STS. Admin can add, edit and delete unsold cars, upload/manage their photos, manually record an in-person sale, and review the sales/payment history. Preserve completed sales and block deletion of a car with a sale record. Keep MySQL as the only application database, Java 17, root Maven Wrapper, the Bootstrap showroom identity and STS support.

## Implementation approach

Spring Boot serves the public storefront, login, admin dashboard and APIs. Use Spring Security with a single environment-configured Admin account (`ADMIN_USERNAME`, `ADMIN_PASSWORD`); never hard-code or commit credentials. Require an authenticated Admin role for `/admin` and `/api/admin/**`; use CSRF protection for state-changing requests. Preview cookies use `SameSite=None; Secure`; document local HTTP STS overrides (`SESSION_COOKIE_SECURE=false`, `SESSION_COOKIE_SAMESITE=Lax`).

Keep `/api/vehicles` and vehicle photos public-read-only, return AVAILABLE cars only, and never expose VIN publicly. Add `/new-cars` and `/used-cars` routes filtered by saved `conditionType`. Checkout collects buyer name/email and optional phone, then a fake payment API accepts only demo methods and outcomes. Under one MySQL transaction, lock and re-check the vehicle; reuse/create the customer and store a payment-attempt row. On success, create a sale row and mark the vehicle SOLD. On failure, save the failed demo-payment attempt only and leave the vehicle available. The UI displays “demo payment—no real payment is processed.” Admin can separately record an offline sale; it uses the existing sales/customer tables and is clearly labeled offline.

Move vehicle create/edit/delete and manual-sale operations behind Admin authorization. Reuse existing MySQL vehicles, photos, customers and sales tables. Add an additive Flyway V5 `demo_payment_attempts` table linked to vehicle/customer and, on success, the sale row; add a sale-channel value to distinguish `DEMO_PAYMENT` from `OFFLINE`. Keep existing sales history; no destructive migration or sample purchases. Admin can add/remove photos and delete only available cars with no sale, payment-attempt, or service-task history.

## Project structure

- `pom.xml` — Java 17 Spring Boot, Spring Security, Validation, Spring Data JPA, MySQL Connector/J, and Flyway; no second database.
- `src/main/java/com/autoadmin/security/` — environment-configured Admin login, authorization, CSRF and cookie policy.
- `src/main/java/com/autoadmin/vehicle/` — public condition-filtered reads, Admin CRUD and validation.
- `src/main/java/com/autoadmin/sales/`, `customer/`, and `payment/` — purchase lock/transaction, fake payment attempts, offline sale entry, and safe response DTOs.
- `src/main/java/com/autoadmin/vehicle/photo/` — MySQL `MEDIUMBLOB` photo storage and Admin management APIs.
- `src/main/resources/static/` — Home, New Cars, Second-Hand Cars, Services, Contact, Admin login/dashboard, responsive Bootstrap views and JavaScript.
- `src/main/resources/db/migration/V5__demo_payment_attempts.sql` — additive payment-attempt schema.
- `src/main/resources/static/manus-routes.json` — complete public, login and admin page route set.
- `src/test/java/` — validation, authorization, purchase concurrency/status, payment success/failure, and photo tests without writing to shared MySQL.

## Design direction

**Movement:** Contemporary premium dealership editorial. **Principles:** car photography first; precise model/spec display; separate public browsing and back-office operations; transparent transaction language. **Color:** graphite and warm ivory provide showroom contrast; champagne brass marks trims and primary actions. **Layout:** compact sidebar with Home followed by New Cars and Second-Hand Cars; image-led inventory pages; one operational Admin workbench with inventory and sales/payment tabs. **Signature elements:** condition ribbons, brass dividers, mono specification labels. **Interaction:** public users compare and confirm a demo Buy flow; Admin logs in before changing stock or recording offline sales. **Animation:** restrained card feedback and reduced-motion support. **Typography:** Inter and IBM Plex Mono with an editorial serif accent. **Brand essence:** “A considered showroom for new and pre-owned cars”—professional, clear, dependable. **Voice:** “Compare the details. Choose your next drive.” and “Every sale, recorded clearly.” **Wordmark:** retain the angular A road mark and “MOTORS · NEW & PRE-OWNED.” **Signature color:** muted champagne brass.

## Local configuration and data safety

In STS Run Configurations → Environment, set `ADMIN_USERNAME` and a private `ADMIN_PASSWORD`; set `SESSION_COOKIE_SECURE=false` and `SESSION_COOKIE_SAMESITE=Lax` for local HTTP. Preview defaults to secure HTTPS cookie settings. Database and payment attempts remain MySQL-only. The managed database is shared by development and production: do not create sample sales or test-buy cars during validation. Use unit tests/mocks for payment success/failure and read-only Preview checks only.
