package com.jeliiadesina.drone.query;

public interface MedicationQuery {
    static String insertMedication() {
        return "INSERT INTO medications (uuid, name, weight, code, created_date, last_modified_date ) VALUES($1, $2, $3, $4, current_timestamp, current_timestamp)";
    }

    static String count() {
        return "SELECT count(*) FROM medications";
    }

    static String countByNameQuery() {
        return "SELECT count(*) FROM medications WHERE name = $1";
    }

    static String selectOneByName() {
        return "SELECT uuid as id, name, weight, code, image, drone_uuid as drone_id FROM medications " +
            "WHERE name = $1 LIMIT 1";
    }

    static String selectOneById() {
        return "SELECT uuid as id, name, weight, code, image, drone_uuid as drone_id FROM medications " +
            "WHERE uuid = $1 LIMIT 1";
    }

    static String selectAllByDroneId() {
        return "SELECT uuid as id, name, weight, code, image, drone_uuid as drone_id FROM medications " +
            "WHERE drone_uuid = $1";
    }

    static String updateWithImage() {
        return "UPDATE medications SET image = $2, last_modified_date = current_timestamp " +
            "WHERE name = $1 RETURNING *";
    }

    static String updateWithDroneId() {
        return "UPDATE medications SET drone_uuid = $2, last_modified_date = current_timestamp " +
            "WHERE uuid = $1 RETURNING *";
    }

    static String selectTotalMedicationWeigh() {
        return "SELECT drone_uuid as drone_id, SUM(weight) as total_weight FROM medications " +
            "WHERE (drone_uuid = $1) GROUP BY drone_uuid";
    }

    static String selectAll() {
        return "SELECT uuid as id, name, weight, code, image, drone_uuid as drone_id FROM medications";
    }

}
