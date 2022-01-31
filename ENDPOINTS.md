## REST API
### Register a Drone

### Request

`POST /register`

    http POST :8080/register <<< '{
        "serialNumber": "drone-03",
        "model": "Middleweight",
        "weightLimit": 88,
        "batteryCapacity": 50
    }'

### Response

    HTTP/1.1 200 OK
    content-length: 133

### Get All Drones
### Request

`GET /drones`

    http :8080/drones

### Response

    HTTP/1.1 200 OK
    content-length: 186
    
    [ {
        "serialNumber" : "drone-03",
        "model" : "Middleweight",
        "weightLimit" : 88.0,
        "batteryCapacity" : 50.0,
        "state" : "IDLE"
    } ]

### Get a specific Drone
### Request

`GET /drones/:serialNumber`

    http :8080/drones/drone-03

### Response

    HTTP/1.1 200 OK
    content-length: 182
    
    {
        "serialNumber" : "drone-03",
        "model" : "Middleweight",
        "weightLimit" : 88.0,
        "batteryCapacity" : 50.0,
        "state" : "IDLE"
    }

### Create a Medication

### Request

`POST /medications`

    http POST :8080/medications <<< '{
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

    http :8080/medications

### Response

    HTTP/1.1 200 OK
    content-length: 186
    
    [ {
    "name" : "medication-01",
    "weight" : 2.0,
    "code" : "SAMPLE",
    "image" : null,
    "droneId" : null
    } ]

### Get a specific Medication
### Request

`GET /medications/:name`

    http :8080/medications/medication-01

### Response

    HTTP/1.1 200 OK
    content-length: 182
    
    {
        "name" : "medication-01",
        "weight" : 2.0,
        "code" : "SAMPLE",
        "image" : null,
        "droneId" : null
    }

### Get All available drones Drone
### Request

`GET /available`

    http :8080/available

### Response

    HTTP/1.1 200 OK
    content-length: 194
    
    [ {
        "serialNumber" : "drone-03",
        "model" : "Middleweight",
        "weightLimit" : 88.0,
        "batteryCapacity" : 50.0,
        "state" : "IDLE"
    } ]

### Load a Medication to a Drone

### Request

`POST /drones/:serialNumber/medications`

    http POST :8080/drones/drone-03/medications <<< '{
        "name": "medication-01"
    }'

### Response

    HTTP/1.1 200 OK
    content-length: 61

### Get All loaded Medications to a specific Drone
### Request

`GET /drones/:serialNumber/medications`

    http :8080/drones/drone-03/medications

### Response

    HTTP/1.1 200 OK
    content-length: 194
    
    [ {
        "name" : "medication-01",
        "weight" : 2.0,
        "code" : "SAMPLE",
        "image" : null,
        "droneId" : "33b53000-fb62-4dd1-9afb-9a987bb69d5c"
    } ]