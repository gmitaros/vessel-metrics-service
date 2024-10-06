# Vessel Metrics Service

## Overview

The **Vessel Metrics Service** is an application designed for parsing, validating, and analyzing vessel data from CSV files, as well as generating compliance and performance metrics. The service supports calculating speed differences, fuel efficiency, outlier detection, and more. The system is built with **Spring Boot 3.x** and **Java 21**, leveraging object-oriented principles, error handling, and performance optimization.

## Features

1. **Speed Difference Calculation**: Calculate the difference between actual speed over ground and proposed speed over ground for each vessel waypoint.
2. **Validation Issues**: Identify and report validation issues such as missing values, negative speeds, and outliers.
3. **Compliance Comparison**: Compare two vessels' compliance based on actual and proposed speeds.
4. **Problematic Waypoints**: Identify groups of consecutive waypoints with validation issues and classify problems (e.g., outliers, missing values).
5. **Data Merging**: Retrieve raw and calculated metrics for a specific vessel within a given time frame.

## Requirements

- **Java 21**
- **Spring Boot 3.x**
- **Docker** (for PostgreSQL database setup)
- **Maven** (for dependency management)

## Setup Instructions

1. **Clone the repository**:
   ```bash
   git clone https://github.com/gmitaros/vessel-metrics-service.git
   cd vessel-metrics-service
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

### Running with Docker
To run the service along with PostgreSQL using Docker and Docker Compose:

1. Build the Docker image:
    ```bash
    docker-compose build
    ```

2. Start the application:
    ```bash
    docker-compose up
    ```

This will start both the PostgreSQL container and the vessel-metrics-service container.
### Environment Variables
These are configured via the `docker-compose.yml` file and passed as environment variables to the application:
- `SPRING_DATASOURCE_URL`: The JDBC URL for the PostgreSQL database.
- `SPRING_DATASOURCE_USERNAME`: The username for the database.
- `SPRING_DATASOURCE_PASSWORD`: The password for the database.


### Application Properties

The application is configured using the `application.properties` file. Below are key configurations that are customizable:

- `spring.application.name`: Defines the name of the application (`Vessel Metrics Service`).
- `vessel.metrics.outlier.threshold`: Sets the threshold for detecting outliers in vessel data. Default is 3.0.

#### Database Configuration (PostgreSQL)
- `spring.datasource.url`: The JDBC URL for the PostgreSQL database.
- `spring.datasource.username`: Database username.
- `spring.datasource.password`: Database password.

#### JPA & Hibernate Configuration
- `spring.jpa.hibernate.ddl-auto`: Disables automatic DDL generation by Hibernate (`none`).
- `spring.jpa.show-sql`: Controls whether to show SQL queries in the log (`false`).
- `spring.jpa.properties.hibernate.jdbc.batch_size`: Sets the batch size for Hibernate inserts (`1000`).
- `spring.datasource.hikari.maximum-pool-size`: Configures the HikariCP connection pool size (set to `20`).

#### CSV Data Loading
- `vessel.metrics.csv.load`: Determines whether to load CSV data at application startup (`false`).

You can modify these properties in `application.properties` located in `src/main/resources/`.


## Assumptions

- The CSV file is placed in the `/data/` directory with the required fields (e.g., `vessel_code`, `datetime`, `latitude`, etc.).
- Thresholds for outlier detection and other validations can be adjusted via properties in the `application.yml` file.

## Error Handling

- Comprehensive error handling is implemented across services. Errors like missing vessel data, invalid inputs, and calculation failures are logged and managed using custom exceptions.
- API responses are designed to return meaningful error messages and HTTP status codes (e.g., 404 for missing vessels, 400 for invalid input).

## Logging

- Every major service and controller action is logged, including metrics calculations, data validation, and compliance comparisons.

## Unit Tests

- The project contains unit tests for critical components, ensuring that speed differences, fuel efficiency, and validation services function correctly.
- To run the tests:
  ```bash
  mvn test
  ```

## Dependencies

The project relies on the following external libraries:

1. **Spring Boot 3.x** - Core framework.
2. **PostgreSQL** - Relational database.
3. **Hibernate** - ORM for interacting with PostgreSQL.
4. **Lombok** - To reduce boilerplate code.
5. **Apache Commons CSV** - For CSV parsing.
6. **Flyway** - Database migrations.

## API Endpoints

### 1. Speed Differences
- **GET** `/vessels/{vesselCode}/speed-differences`
- Returns paginated speed differences for a vessel.

### 2. Validation Issues
- **GET** `/vessels/{vesselCode}/validation-issues`
- Returns validation issues for a vessel, sorted by frequency.

### 3. Compliance Comparison
- **GET** `/vessels/compare-compliance?vesselCode1=code1&vesselCode2=code2`
- Compares compliance between two vessels.

### 4. Data Merge
- **GET** `/vessels/{vesselCode}/data-merge?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD`
- Retrieves merged raw and calculated metrics within a specific period.

### 5. Problematic Waypoints
- **GET** `/vessels/{vesselCode}/problematic-waypoints?problemType=outlier`
- Returns problematic waypoints grouped by validation issues (optional problem type filter).
