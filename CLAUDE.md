# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0.0 application (package: `com.magic_fans.wizards`) using Java 17. The project uses Maven for build management and includes Spring Web and Thymeleaf for web development.

## Build and Development Commands

### Build the project
```bash
mvn clean package
```

### Run the application
```bash
mvn spring-boot:run
```
The application runs on `http://localhost:8080` by default.

### Run tests
```bash
mvn test
```

### Run a single test
```bash
mvn test -Dtest=WizardsApplicationTests
```

### Lint and format
```bash
mvn clean compile
```

### Clean build artifacts
```bash
mvn clean
```

## Project Structure

- `src/main/java/com/magic_fans/wizards/` - Main application code
  - `WizardsApplication.java` - Spring Boot entry point
- `src/main/resources/application.yaml` - Application configuration
- `src/main/resources/static/` - Static assets (CSS, JavaScript, images)
- `src/main/resources/templates/` - Thymeleaf templates for web pages
- `src/test/java/com/magic_fans/wizards/` - Unit tests

## Key Technologies

- **Framework**: Spring Boot 4.0.0 with Spring Web
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven
- **Java Version**: 17
- **Testing**: JUnit Jupiter (provided by spring-boot-starter-test)

## Important Notes

- The original package name `com.magic-fans.wizards` was invalid and the project uses `com.magic_fans.wizards` instead.
- The application is configured to run as a standard Spring Boot application with no special profiles configured.