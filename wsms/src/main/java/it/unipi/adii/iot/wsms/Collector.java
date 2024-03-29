package it.unipi.adii.iot.wsms;

import it.unipi.adii.iot.wsms.services.MQTT;
import it.unipi.adii.iot.wsms.services.RegistrationService;
import it.unipi.adii.iot.wsms.services.DBService;
import it.unipi.adii.iot.wsms.utils.Parameters;

public class Collector {
    private static DBService th = DBService.getInstance();
    private static MQTT mc;
    private static RegistrationService rs;

    public static void main(String[] args) {

        try {
            //th.cleanDB();

            mc = new MQTT();
            rs = new RegistrationService();
            rs.start();

            Parameters.setInitTime();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
