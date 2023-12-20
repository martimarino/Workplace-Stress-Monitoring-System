package it.unipi.adii.iot.wsms;

import java.util.logging.LogManager;

import it.unipi.adii.iot.wsms.services.MQTT;
import it.unipi.adii.iot.wsms.services.RegistrationService;
import it.unipi.adii.iot.wsms.services.DBService;


public class Collector {
    private static DBService th = DBService.getInstance();


    public static void main(String[] args) {
        // Remove log messages (Californium)
        LogManager.getLogManager().reset();

        th.cleanDB();

        MQTT mc = new MQTT();
        RegistrationService rs = new RegistrationService();

        rs.start();

    }

}
