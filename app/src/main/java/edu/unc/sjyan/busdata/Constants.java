package edu.unc.sjyan.busdata;

/**
 * Created by Tamzeed on 3/28/16.
 */
public  class Constants {
    public static final String USER = "chenchik";
    public static String header = "Time" + "," + "Temp" + "," + "Hum" + "," + "Baro" +
            "," + "Acc(x)" + "," + "Acc(y)" + "," + "Acc(z)" +
            "," + "LinAcc(x)" + "," + "LinAcc(y)" + "," + "LinAcc(z)" +
            "," + "Mag(x)" + "," + "Mag(y)" + "," + "Mag(z)" + "," +  "Degrees" + "," + "Turn" +
            "," + "Gyro(x)" + "," + "Gyro(y)" + "," + "Gyro(z)" +
            "," + "Light" + "," + "Lat" + "," + "Long" + "," + "Stop";
    public static String acceloString = "-9999,-9999,-9999";
    public static String linAcceloString = "-9999,-9999,-9999";
    public static String magnet = "-9999,-9999,-9999";
    public static String degrees = "-9999";
    public static String gyroString = "-9999,-9999,-9999";
    public static String temperature = "-9999";
    public static String humidity = "-9999";
    public static  String baroString = "-9999";
    public static String lightString = "-9999";
    public static String stopString = "-9999";
    public static String latString = "-9999";
    public static String longString = "-9999";
    public static String turnString = "-9999";
    public static String ALL_SENSOR_STR = "-9999,-9999,-9999,-9999,-9999,-9999," +
            "-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999,-9999";

    //dc constants
    public static int turnThreshold = 50;
    public static int turnDegreeAmount = 40;
    public static int zeroTo360DegreeAmount = 300;
    public static String[] degreeArray = new String[turnThreshold];

}
