Drone App
=

## Introduction
There is a major new technology that is destined to be a disruptive force in the field of transportation: **the drone**. Just as the mobile phone allowed developing countries to leapfrog older technologies for personal communication, the drone has the potential to leapfrog traditional transportation infrastructure.

Useful drone functions include delivery of small items that are (urgently) needed in locations with difficult access.


## Stack
- Java 16
- Vertx
- Postgresql
- Gradle
- Vertx-Junit5

## Building
To build this project, run the below command from your terminal:
```
./gradlew build
```

## Testing
To test this project, run the below command from your terminal:
```
./gradlew test
```

## Running
To run this project, run the below command from your terminal:
```
java -jar app/build/libs/app-all.jar
```

## REST API
### Register a Drone

### Request

`POST /drones`

    http POST :8080/api/v1/drones <<< '{
        "serialNumber": "drone-03",
        "model": "Middleweight",
        "weightLimit": 88,
        "batteryCapacity": 50
    }'

### Response

    HTTP/1.1 200 OK
    content-length: 133
    
    {
        "serialNumber" : "drone-03",
        "model" : "Middleweight",
        "weightLimit" : 88.0,
        "batteryCapacity" : 50.0,
        "state" : "IDLE"
    }

### Get All Drones
### Request

`GET /drones`

    http :8080/api/v1/drones

### Response

    HTTP/1.1 200 OK
    content-length: 186
    
    [ {
        "id" : "33b53000-fb62-4dd1-9afb-9a987bb69d5c",
        "serialNumber" : "drone-03",
        "model" : "Middleweight",
        "weightLimit" : 88.0,
        "batteryCapacity" : 50.0,
        "state" : "IDLE"
    } ]

### Get a specific Drone
### Request

`GET /drones/:serialNumber`

    http :8080/api/v1/drones/drone-03

### Response

    HTTP/1.1 200 OK
    content-length: 182
    
    {
        "id" : "33b53000-fb62-4dd1-9afb-9a987bb69d5c",
        "serialNumber" : "drone-03",
        "model" : "Middleweight",
        "weightLimit" : 88.0,
        "batteryCapacity" : 50.0,
        "state" : "IDLE"
    }

### Create a Medication

### Request

`POST /medications`

    http POST :8080/api/v1/medications <<< '{
        "name": "medication-01",
        "code": "SAMPLE",
        "weight": 2
    }'

### Response

    HTTP/1.1 200 OK
    content-length: 87
    
    {
        "name" : "medication-01",
        "weight" : 2.0,
        "code" : "SAMPLE",
        "image" : null
    }

### Get All Medications
### Request

`GET /medications`

    http :8080/api/v1/medications

### Response

    HTTP/1.1 200 OK
    content-length: 186
    
    [ {
    "id" : "4e683570-5cbe-4f3e-b3c4-f311a3011d29",
    "name" : "medication-01",
    "weight" : 2.0,
    "code" : "SAMPLE",
    "image" : null,
    "droneId" : null
    } ]

### Get a specific Medication
### Request

`GET /medications/:name`

    http :8080/api/v1/medications/medication-01

### Response

    HTTP/1.1 200 OK
    content-length: 182
    
    {
        "id" : "4e683570-5cbe-4f3e-b3c4-f311a3011d29",
        "name" : "medication-01",
        "weight" : 2.0,
        "code" : "SAMPLE",
        "image" : null,
        "droneId" : null
    }

### Get All available drones Drone
### Request

`GET /drones?state=AVAILABLE`

    http :8080/api/v1/drones?state=AVAILABLE

### Response

    HTTP/1.1 200 OK
    content-length: 194
    
    [ {
        "id" : "33b53000-fb62-4dd1-9afb-9a987bb69d5c",
        "serialNumber" : "drone-03",
        "model" : "Middleweight",
        "weightLimit" : 88.0,
        "batteryCapacity" : 50.0,
        "state" : "IDLE"
    } ]

### Load a Medication to a Drone

### Request

`POST /drones/:serialNumber/medications`

    http POST :8080/api/v1/drones/drone-03/medications <<< '{
        "name": "medication-01"
    }'

### Response

    HTTP/1.1 200 OK
    content-length: 61
    
    {
        "name" : "medication-01",
        "serialNumber" : "drone-03"
    }

### Get All loaded Medications to a specific Drone
### Request

`GET /drones/:serialNumber/medications`

    http :8080/api/v1/drones/drone-03/medications

### Response

    HTTP/1.1 200 OK
    content-length: 194
    
    [ {
        "id" : "4e683570-5cbe-4f3e-b3c4-f311a3011d29",
        "name" : "medication-01",
        "weight" : 2.0,
        "code" : "SAMPLE",
        "image" : null,
        "droneId" : "33b53000-fb62-4dd1-9afb-9a987bb69d5c"
    } ]