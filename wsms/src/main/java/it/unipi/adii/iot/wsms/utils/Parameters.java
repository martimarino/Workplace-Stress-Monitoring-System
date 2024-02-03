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

    public static final int LOWER_BOUND_BRIGHT = 10; // in dB
    public static final int UPPER_BOUND_BRIGHT = 60; // in dB
    public static final int COMFORT_BRIGHT = 30;	 // in dB


    public static int getLowerBound(String dataType) {
        switch (dataType) {
            case "temperature":
                return LOWER_BOUND_TEMP;
            case "humidity":
                return LOWER_BOUND_HUM;
            case "noise":
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
            case "noise":
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
            case "noise":
                return COMFORT_BRIGHT;
            default:
                return -1;
        }
    }


    static int hour = 7;

    public Timestamp adjustTime(Timestamp ts) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);

        hour = (hour+1)%24;

        return new Timestamp(cal.getTime().getTime());

    }

}
