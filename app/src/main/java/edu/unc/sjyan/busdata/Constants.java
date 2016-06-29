package edu.unc.sjyan.busdata;

/**
 * Created by Tamzeed on 3/28/16.
 */
public  class Constants {
    public static String header = "Time" + "\t" + "Temp" + "\t" + "Hum" + "\t" + "Baro" +
            "\t" + "Acc(x)" + "\t" + "Acc(y)" + "\t" + "Acc(z)" + "\t" + "Mag(x)" +
            "\t" + "Mag(y)" + "\t" + "Mag(z)" + "\t" + "Gyro(x)" + "\t" + "Gyro(y)" +
            "\t" + "Gyro(z)" + "\t" + "Light" + "\t" + "Stop";
    public static String fileName = "";
    public static String deviceName = "";
    public static String experimentType = "";
    public static String temperature = "-9999";
    public static String humidity = "-9999";
    public static  String baroString = "-9999";
    public static String acceloString = "-9999\t-9999\t-9999";
    public static String magnet = "-9999\t-9999\t-9999";
    public static String gyroString = "-9999\t-9999\t-9999";
    public static String lightString = "-9999";
    public static String stopString = "-9999";
    //public static ArrayList<String> list= new ArrayList<String>();
    public static long AUDIO_FILE_DURATION_MS = 60000;
    public static long TIMER_INTERVAL_MS = 60000;
    public static String ALL_SENSOR_STR = "-9999\t-9999\t-9999\t-9999\t-9999\t-9999\t" +
            "-9999\t-9999\t-9999\t-9999\t-9999\t-9999\t-9999\t-9999\t-9999";

}
