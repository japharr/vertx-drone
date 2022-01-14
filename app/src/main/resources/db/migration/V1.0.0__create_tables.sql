CREATE TABLE drones (
uuid VARCHAR(500) NOT NULL,
serial_number VARCHAR(100) NOT NULL,
model VARCHAR(100) NOT NULL,
weight_limit DECIMAL NOT NULL,
battery_capacity DECIMAL NOT NULL,
state VARCHAR(100) NOT NULL,
created_date TIMESTAMP,
last_modified_date TIMESTAMP
);

ALTER TABLE drones ADD CONSTRAINT pk_drone_id PRIMARY KEY (uuid);

CREATE TABLE medications (
uuid VARCHAR(500) NOT NULL,
name VARCHAR(100) NOT NULL,
code VARCHAR(100) NOT NULL,
weight DECIMAL NOT NULL,
image VARCHAR(100),
drone_uuid VARCHAR(500),
created_date TIMESTAMP,
last_modified_date TIMESTAMP
);

ALTER TABLE medications ADD CONSTRAINT pk_medication_id PRIMARY KEY (uuid);