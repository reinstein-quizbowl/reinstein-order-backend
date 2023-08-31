This is a Spring Boot app written in Kotlin.

# Running
Several environment variables are required to run this app. They are documented in `src/main/kotlin/com/reinsteinquizbowl/order/util/Config.kt`. They define the port the server runs on, database credentials, credentials for other external services (currently just Sendgrid), and more.

In development, the app can be run using `./gradlew bootRun`.

On production, we build a jar using `./gradlew bootJar`, then deploy it using `scripts/startup.bash` (or `scripts/restart.bash`). It is presumed that production environments will have a reverse proxy layer that includes HTTPS management.

Detekt is installed for code quality. Run it using `./gradlew detekt`. This generates reports in `build/reports/detekt/`.

# Database
The app uses a Postgres database (mapped using Spring Data, JPA, and Hibernate).

Relevant scripts are in `resources/db/`. For now, there is no formal migration process; the scripts in that directory are updated to reflect schema changes that are applied separately. 

# Security
The app uses JWTs for authentication and authorization. Only some endpoints require authorization; they are annotated with `@PreAuthorize("hasAuthority('admin')")`.

For now, there is only one role (`admin`), which is automatically attached to every user. We don't currently anticipate a need for distinguishing roles, but one can easily be added later if necessary.

Adding a user currently requires a manual `insert into account…` with a BCrypt hash you compute separately. No programmatic facility is provided for this because we anticipate adding users to be extraordinarily rare (possibly never other than for initial deployment).

The auth setup was largely accomplished by following [this tutorial](https://www.bezkoder.com/spring-boot-react-jwt-auth/) (with some adaptations).
