# AutoAdmin Motors

AutoAdmin Motors is a Java Spring Boot dealership storefront with Bootstrap 5.3.8, HTML, CSS, and JavaScript. The public pages include Home, separate New Cars and Second-Hand Cars collections, Services, and Contact Us. A username/password-protected Admin Dashboard manages MySQL inventory, photos, sales and demo-payment history. Public visitors can run a simulated Card or UPI purchase; this demo does not process money or collect payment credentials.

## Import and run in Spring Tool Suite

1. Install JDK 17 or newer and MySQL Server. Create the `autoadmin` database before launching the app:

   ```sql
   CREATE DATABASE autoadmin CHARACTER SET utf8mb4;
   CREATE USER 'autoadmin_user'@'localhost' IDENTIFIED BY 'choose-a-local-password';
   GRANT ALL PRIVILEGES ON autoadmin.* TO 'autoadmin_user'@'localhost';
   ```

2. In STS, choose **File → Import… → Maven → Existing Maven Projects → Next**.
3. Select the project root folder containing `pom.xml`, select that `pom.xml`, and click **Finish**. Wait for Maven dependencies to download.
4. Expand `src/main/java`, open `com.autoadmin.AutoAdminApplication.java`, then choose **Run As → Spring Boot App**.
5. In **Run Configurations → Environment**, set `DB_USERNAME=autoadmin_user` and `DB_PASSWORD` to the local database user's password. Also set `ADMIN_USERNAME` and a private `ADMIN_PASSWORD` of at least 12 characters. For plain-HTTP `http://localhost:8090`, set `SESSION_COOKIE_SECURE=false` and `SESSION_COOKIE_SAMESITE=Lax`. HTTPS Preview defaults to `SESSION_COOKIE_SECURE=true` and `SESSION_COOKIE_SAMESITE=None`. `DATABASE_URL` is optional for local STS because it defaults to `jdbc:mysql://localhost:3306/autoadmin`. Do not commit passwords to source control.
6. Open `http://localhost:8090`. The `PORT` environment variable overrides the default port.

The project uses a root-level Maven `pom.xml`, conventional `src/main/java` and `src/main/resources` folders, and an included `mvnw.cmd` wrapper. If running from PowerShell instead of STS, configure the same environment variables and run `.\mvnw.cmd spring-boot:run` from the project root.

## Showroom and listing behavior

- Home lists available cars from MySQL. Dedicated **New Cars** and **Second-Hand Cars** sidebar pages filter by condition and include their own search.
- Admin creates and edits vehicle inventory. New cars require make, model, model year, variant, fuel/powertrain, transmission, and engine/motor details. Power, torque, efficiency/range, drivetrain, seating, warranty, colour, price and photos can also be recorded. VIN is not required for New dealer-stock listings.
- Second-Hand cars require a VIN/chassis number and show mileage, colour, asking price, and photos. VIN is never returned in public listing or photo responses.
- Each listing’s **View details** modal displays saved model and vehicle specifications. Public listing responses never return VIN.
- Every available listing has a **Buy** button. The demo checkout collects buyer contact details only, offers simulated Card/UPI and success/failure choices, and clearly states that no real payment or money movement occurs. A successful simulation stores an attempt and sale and marks the vehicle `SOLD`; a failed attempt is saved without creating a sale, so the car remains `AVAILABLE`.
- The protected Admin Dashboard supports inventory create/edit/delete, adding/removing photos, manually recording offline sales, and viewing completed sales plus all demo payment attempts. Cars with sale, payment, or service history cannot be deleted. CSRF protection applies to state-changing requests.
- Photo upload remains optional: up to five JPEG, PNG, or WebP files, at most 2 MB each. Image bytes are checked and stored in the MySQL `vehicle_photos` `MEDIUMBLOB` table with the listing. No photos are saved to a local folder or external storage service.
- Car model/specification data is entered through the listing form and stored in MySQL. No sample or assumed manufacturer specifications are seeded.

## Bootstrap styling

The pages use Bootstrap 5.3.8's stylesheet and bundled JavaScript from pinned jsDelivr CDN URLs with Subresource Integrity hashes. The [official Bootstrap 5.3 introduction](https://getbootstrap.com/docs/5.3/getting-started/introduction/) documents these CDN assets and integrity values; the [official versions page](https://getbootstrap.com/docs/versions/) identifies v5.3.8 as the current v5 release. AutoAdmin's custom `styles.css` loads afterward to keep its dealership theme. The showroom hero photo is available through project storage in Preview; if that route is unavailable in STS, the page falls back to the bundled `src/main/resources/static/images/showroom-hero.jpg`, sourced from [Unsplash's car-showroom collection](https://unsplash.com/s/photos/car-showroom). A network connection is needed to load Bootstrap; no npm installation or extra Maven dependency is required.

## MySQL behavior

MySQL Connector/J is the only database driver. Flyway applies the versioned, additive SQL migrations in `src/main/resources/db/migration`; Hibernate uses `ddl-auto=validate` and does not create or alter tables itself. The database schema must exist before startup, and the configured user must have permission for Flyway's table migrations. V4 defaults existing records to `USED`, preserves existing VINs, makes VIN nullable for New listings, and adds specification columns without deleting vehicle data. V5 adds a sale-channel field and the `demo_payment_attempts` history table without deleting existing records. Startup fails rather than switching to another database or sample data if MySQL is unavailable or the schema does not match.

## Pages and API

- `/` — Home; available inventory, condition filters, details and Buy.
- `/new-cars` — New vehicle listings and specifications.
- `/used-cars` — Second-Hand vehicle listings.
- `/services` — dealership services and contact links.
- `/contact` — form submissions saved in MySQL.
- `/admin/login` — username/password Admin login.
- `/admin` — protected Admin inventory, offline sale and history dashboard.
- `GET /api/vehicles` — available listings and public specifications, without VIN values.
- `POST /api/purchases/{vehicleId}` — demo Card/UPI result; records the attempt and, on success, the sale in MySQL.
- `/api/admin/**` — authenticated Admin inventory, photo, sales and payment history operations.
- `GET /api/vehicles/{vehicleId}/photos` and `GET /api/vehicles/{vehicleId}/photos/{photoId}` — photo metadata and image bytes.
- `POST /api/contact` — persist a Contact Us message.
