This is a Spring Boot app written in Kotlin.

It's the back end to the [ordering interface](https://github.com/jonahgreenthal/reinstein-order-ui) for [Reinstein QuizBowl](https://www.reinsteinquizbowl.com).

The production instance is running at `order-api.reinsteinquizbowl.com`.

# Running
Several environment variables are required to run this app. They are documented in `src/main/kotlin/com/reinsteinquizbowl/order/util/Config.kt`. They define the port the server runs on, database credentials, credentials for other external services (currently just email), and more.

In development, the app can be run using `./gradlew bootRun`.

On production, we build a JAR using `./gradlew bootJar`, then deploy it using `scripts/startup.bash` (or `scripts/restart.bash`). It is presumed that production environments will have a reverse proxy layer that includes HTTPS management.

Detekt is installed for code quality. Run it using `./gradlew detekt`. This generates reports in `build/reports/detekt/`.

# Database
The app uses a Postgres database (mapped using Spring Data, JPA, and Hibernate).

Relevant scripts are in `resources/db/`. For now, there is no formal migration process; the scripts in that directory are updated to reflect schema changes that are applied separately. 

# Security
The app uses JWTs for authentication and authorization. Only some endpoints require authorization; they are annotated with `@PreAuthorize("hasAuthority('admin')")`.

For now, there is only one role (`admin`), which is automatically attached to every user. We don't currently anticipate a need for distinguishing roles, but that can easily be added later if necessary.

Adding a user currently requires a manual `insert into account…` with a BCrypt hash you compute separately. No programmatic facility is provided for this because we anticipate adding users to be extraordinarily rare (possibly never other than for initial deployment).

The auth setup was largely accomplished by following [this tutorial](https://www.bezkoder.com/spring-boot-react-jwt-auth/) (with some adaptations).

# License
The purpose of posting this code publicly is to serve as a portfolio item for its developer, [Jonah Greenthal](https://www.github.com/jonahgreenthal). The code is owned by Reinstein QuizBowl and is not licensed for other use, but you're welcome to look at it, and if you want to do something with it, write to [admin@reinsteinquizbowl.com](mailto:admin@reinsteinquizbowl.com).
