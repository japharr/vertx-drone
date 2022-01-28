package com.jeliiadesina.drone.query;

public interface DroneQuery {
    static String insertDrone() {
        return "INSERT INTO drones VALUES($1, $2, $3, $4, $5, $6, current_timestamp, current_timestamp)";
    }

    static String count() {
        return "SELECT count(*) FROM drones";
    }

    static String countBySerialNumber() {
        return "SELECT count(*) FROM drones WHERE serial_number = $1";
    }

    static String countByIdQuery() {
        return "SELECT count(*) FROM drones WHERE uuid = $1";
    }

    static String selectOneBySerialNumber() {
        return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones " +
            "WHERE serial_number = $1 LIMIT 1";
    }
    static String selectOneById() {
        return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones " +
            "WHERE id = $1 LIMIT 1";
    }

    static String selectAll() {
        return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones";
    }

    static String selectDronesByState() {
        return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones " +
            "WHERE state = $1";
    }

    static String selectAvailableDronesByState() {
        return "SELECT uuid as id, serial_number, model, weight_limit, battery_capacity, state FROM drones " +
            "WHERE (state = 'IDLE' OR state = 'LOADING') AND battery_capacity > 25";
    }

    static String updateWithState() {
        return "UPDATE drones SET state = $2, last_modified_date = current_timestamp " +
            "WHERE uuid = $1 RETURNING *";
    }
}
