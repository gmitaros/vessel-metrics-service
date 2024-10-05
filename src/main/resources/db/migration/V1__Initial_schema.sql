CREATE TABLE vessel_data
(
    id                         BIGSERIAL PRIMARY KEY,
    vessel_data_uuid           VARCHAR(255) NOT NULL UNIQUE,
    vessel_code                VARCHAR(255) NOT NULL,
    date_time                  TIMESTAMP    NOT NULL,
    latitude                   DOUBLE PRECISION,
    longitude                  DOUBLE PRECISION,
    power                      DOUBLE PRECISION,
    fuel_consumption           DOUBLE PRECISION,
    actual_speed_overground    DOUBLE PRECISION,
    proposed_speed_overground  DOUBLE PRECISION,
    predicted_fuel_consumption DOUBLE PRECISION,
    speed_difference           DOUBLE PRECISION,
    fuel_efficiency            DOUBLE PRECISION,
    validation_status          VARCHAR(100) NOT NULL
);

CREATE INDEX idx_vessel_code ON vessel_data (vessel_code);
CREATE INDEX idx_date_time ON vessel_data (date_time);

CREATE TABLE vessel_data_validation_errors
(
    id               BIGSERIAL PRIMARY KEY,
    vessel_data_uuid VARCHAR(255) NOT NULL,
    vessel_code      VARCHAR(255) NOT NULL,
    error_message    VARCHAR(255) NOT NULL,
    problem_type     VARCHAR(255) NOT NULL,
    FOREIGN KEY (vessel_data_uuid) REFERENCES vessel_data (vessel_data_uuid) ON DELETE CASCADE
);

