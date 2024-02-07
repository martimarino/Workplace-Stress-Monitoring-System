package it.unipi.adii.iot.wsms.utils;


import java.sql.Timestamp;
import java.util.Calendar;

public class Parameters {

    private Parameters() {

    }

    public static final int LOWER_BOUND_TEMP = 19; // in °C
    public static final int UPPER_BOUND_TEMP = 25; // in °C
    public static final int COMFORT_TEMP = 22;		// in °C

    public static final int LOWER_BOUND_HUM = 30; // in %
    public static final int UPPER_BOUND_HUM = 60; // in %
    public static final int COMFORT_HUM = 40;	   // in %

    public static final int LOWER_BOUND_BRIGHT = 300; // in lux
    public static final int UPPER_BOUND_BRIGHT = 400; // in lux



    public static int getLowerBound(String dataType) {
        switch (dataType) {
            case "temperature":
                return LOWER_BOUND_TEMP;
            case "humidity":
                return LOWER_BOUND_HUM;
            case "brightness":
                return LOWER_BOUND_BRIGHT;
            default:
                return -1;
        }
    }

    public static int getUpperBound(String dataType) {
        switch (dataType) {
            case "temperature":
                return UPPER_BOUND_TEMP;
            case "humidity":
                return UPPER_BOUND_HUM;
            case "brightness":
                return UPPER_BOUND_BRIGHT;
            default:
                return -1;
        }
    }

    public static int getComfortValue(String dataType) {
        switch (dataType) {
            case "temperature":
                return COMFORT_TEMP;
            case "humidity":
                return COMFORT_HUM;
            default:
                return -1;
        }
    }

    private static Timestamp currentTimestamp;

    public static void setInitTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 45);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        currentTimestamp = new Timestamp(cal.getTime().getTime());
    }



    public static Timestamp adjustTime() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTimestamp);

        cal.add(Calendar.MINUTE, 15);

        currentTimestamp = new Timestamp(cal.getTime().getTime());

        return currentTimestamp;

    }

}
