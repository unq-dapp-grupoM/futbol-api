# Futbol API

This is a Spring Boot project that provides a RESTful API for managing football-related data.

## Features

*   User authentication and authorization using JWT.
*   RESTful endpoints for managing football data.
*   API documentation using Swagger/OpenAPI.
*   Containerized application using Docker.

## Technologies Used

*   **Java 21**
*   **Spring Boot 3.5.5**
    *   Spring Data JPA
    *   Spring Security
    *   Spring Web
*   **JWT (JSON Web Token)**
*   **Swagger/OpenAPI 3**
*   **H2 Database** (for development)
*   **Lombok**
*   **Gradle**
*   **Docker**

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

*   Java 21 or later
*   Gradle 8.x or later
*   Docker (optional)

### Installation

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/unq-dapp-grupoM/futbol-api.git
    ```

2.  **Navigate to the project directory:**

    ```bash
    cd futbol-api
    ```

3.  **Build the project:**

    ```bash
    ./gradlew build
    ```

## API Documentation

The API documentation is generated using Swagger/OpenAPI. Once the application is running, you can access the Swagger UI at:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Running the application

You can run the application using the following Gradle command:

```bash
./gradlew bootRun
```

Alternatively, you can run the application using the generated JAR file:

```bash
java -jar build/libs/futbol-api-0.0.1-SNAPSHOT.jar
```

### Docker

You can also run the application using Docker. First, build the Docker image:

```bash
docker build -t futbol-api .
```

Then, run the Docker container:

```bash
docker run -p 8080:8080 futbol-api
```

## Running tests

To run the tests, use the following Gradle command:

```bash
./gradlew test
```

## Built With

*   [Spring Boot](https://spring.io/projects/spring-boot) - The web framework used
*   [Gradle](https://gradle.org/) - Dependency Management
*   [Swagger](https://swagger.io/) - API Documentation
*   [Docker](https://www.docker.com/) - Containerization

## Authors
### UNQ DAPP Grupo M:
*   **Kevin Stanley**
*   **Matias Galarza**


See also the list of [contributors](https://github.com/unq-dapp-grupoM/futbol-api/contributors) who participated in this project.
