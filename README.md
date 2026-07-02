# Brainiacs Backend

Backend service for the **brainiacs** application, built with [Spring Boot](https://spring.io/projects/spring-boot).

> Frontend repository: this backend is consumed by the `brainiacs` Angular app from the [nx-workspace](https://github.com/mszymczak710/nx-workspace) monorepo. See the frontend README for how to point it at this backend via `proxy.conf.mjs`.

## Tech stack

- **Java 17**
- **Spring Boot 4.0.6**
  - Spring Web MVC
  - Spring Data JPA
  - Spring Security
- **PostgreSQL** as the primary database
- **Flyway** for database migrations
- **springdoc-openapi** for API documentation (Swagger UI)
- **Lombok** to reduce boilerplate
- **Thumbnailator** for image processing
- **Maven** (with Maven Wrapper) as the build tool
- **Checkstyle** + **fmt-maven-plugin** (google-java-format) for code style enforcement

## Prerequisites

- [JDK 17](https://adoptium.net/)
- [PostgreSQL](https://www.postgresql.org/) running locally (or accessible via connection string)
- No need to install Maven globally — this project uses the Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Getting started

### 1. Clone the repository

```sh
git clone https://github.com/mszymczak710/brainiacs-backend.git
cd brainiacs-backend
```

### 2. Configure the database

Create a PostgreSQL database for the project, then configure the connection (typically via `src/main/resources/application.yml` / `application.properties`, or environment variables), for example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/brainiacs
spring.datasource.username=your_username
spring.datasource.password=your_password
```

> Adjust the database name, credentials, and port to match your local PostgreSQL setup.

Flyway will automatically run pending migrations on application startup, so there's no separate manual migration step required for local development.

### 3. Run the application

Using the Maven Wrapper:

```sh
./mvnw spring-boot:run
```

On Windows:

```sh
mvnw.cmd spring-boot:run
```

By default, the application starts on `http://localhost:8080` (adjust `server.port` in your configuration if you've changed it).

## API documentation

Once the application is running, interactive API documentation (Swagger UI) is available at:

```
http://localhost:8080/swagger-ui/index.html
```

The raw OpenAPI spec is served at:

```
http://localhost:8080/v3/api-docs
```

## Building

```sh
./mvnw clean package
```

This produces an executable JAR under `target/`, which can be run with:

```sh
java -jar target/brainiacs-0.0.1-SNAPSHOT.jar
```

## Testing

```sh
./mvnw test
```

The project includes test support for JPA, Spring Security, and Spring MVC via the respective `spring-boot-starter-*-test` dependencies.

## Code quality

### Checkstyle

Code style rules are enforced via `checkstyle.xml` and run as part of the Maven build:

```sh
./mvnw checkstyle:check
```

### Code formatting

Code formatting is enforced with [google-java-format](https://github.com/google/google-java-format) via the `fmt-maven-plugin`:

```sh
# Check formatting
./mvnw fmt:check

# Auto-format the codebase
./mvnw fmt:format
```

## Project structure

```
src/
  main/
    java/          # Application source code
    resources/     # Configuration files, Flyway migrations, static resources
  test/            # Unit and integration tests
checkstyle.xml      # Checkstyle rules configuration
pom.xml             # Maven project configuration
```

## Related repositories

- [nx-workspace](https://github.com/mszymczak710/nx-workspace) — the Nx monorepo containing the `brainiacs` frontend application that consumes this backend, along with the `invoice-generator` app (which uses a separate `json-server` mock API and does not depend on this backend).

## Learn more

- [Spring Boot documentation](https://docs.spring.io/spring-boot/index.html)
- [springdoc-openapi documentation](https://springdoc.org/)
- [Flyway documentation](https://flywaydb.org/documentation/)
