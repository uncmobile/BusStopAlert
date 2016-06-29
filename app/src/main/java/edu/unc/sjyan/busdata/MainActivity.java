package edu.unc.sjyan.busdata;

// Android Packages
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

// Std Java Lib
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    boolean reading;
    static String fileContent = "";
    static int serial = 1;
    TextView sensortxt;
    private long lastUpdate = 0;
    SensorManager sensorManager;
    Sensor tempSensor, humidSensor, acceleroSensor, magnetSensor, gyroscopeSensor,
            barometerSensor, lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensortxt = (TextView) findViewById(R.id.textViewSensors);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {

            int currType = event.sensor.getType();

            if(currType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Constants.temperature = "" + event.values[0];
            }
            else if(currType == Sensor.TYPE_RELATIVE_HUMIDITY) {
                Constants.humidity = "" + event.values[0];
            }
            else if(currType == Sensor.TYPE_ACCELEROMETER) {
                Constants.acceloString = event.values[0] + "\t" + event.values[1] + "\t" +
                        event.values[2];
            }
            else if(currType == Sensor.TYPE_MAGNETIC_FIELD) {
                Constants.magnet = event.values[0] + "\t" + event.values[1] + "\t" +
                        event.values[2];
            }
            else if(currType == Sensor.TYPE_GYROSCOPE) {
                Constants.gyroString = event.values[0] + "\t" + event.values[1] + "\t" +
                        event.values[2];
            }
            else if(currType == Sensor.TYPE_PRESSURE) {
                Constants.baroString = "" + event.values[0];
            }
            else if(currType == Sensor.TYPE_LIGHT) {
                Constants.lightString = "" + event.values[0];
            }

            /*We update the list of sensor values once every second*/
            if(System.currentTimeMillis() - lastUpdate >= 1000) {

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String currentDateandTime = sdf.format(new Date());

                String str = currentDateandTime + "\t" + Constants.temperature + "\t" +
                        Constants.humidity + "\t" + Constants.baroString + "\t" +
                        Constants.acceloString + "\t" + Constants.magnet +
                        "\t" + Constants.gyroString + "\t" + Constants.lightString;

                Constants.ALL_SENSOR_STR = str;
                appendContent(Constants.ALL_SENSOR_STR + "\n");
                //Constants.list.add(str);
                lastUpdate = System.currentTimeMillis();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void sensorKill() {
        sensorManager.unregisterListener(this);
        reading = false;
    }

    public void sensorInit() {
        reading = true;
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);

        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        humidSensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        String senlist = "[";

        appendContent(Constants.header + "\n");

        if(tempSensor != null) {
            sensorManager.registerListener(this, tempSensor, 900);
            senlist += " TEMP ";
        }

        if(humidSensor != null) {
            sensorManager.registerListener(this, humidSensor, 900);
            senlist += " HUM ";
        }

        if(acceleroSensor!=null) {
            sensorManager.registerListener(this, acceleroSensor, 900);
            senlist += " ACC ";
        }

        if(magnetSensor != null) {
            sensorManager.registerListener(this, magnetSensor, 900);
            senlist += " MAG ";
        }

        if(gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, 900);
            senlist += " GYRO ";
        }

        if(barometerSensor != null) {
            sensorManager.registerListener(this, barometerSensor, 900);
            senlist += " BARO ";
        }

        if(lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, 900);
            senlist += " LHT ";
        }

        senlist += "]";
        sensortxt.append(senlist);
    }

    public void clearContent() {
        fileContent = "";
    }

    public void appendContent(String content) {
        fileContent += content;
        Log.v("", content);
    }

    public void generateFile() {
        reading = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(new Date());
            File root = new File(Environment.getExternalStorageDirectory(), today + "_sjyan");
            if (!root.exists()) {
                root.mkdirs();
            }

            sdf = new SimpleDateFormat("HHmmss");
            String fileToWrite = sdf.format(new Date()) + ".txt";
            File file = new File(root, fileToWrite);

            int serialIncrement = 1;
            while(file.exists()) {
                fileToWrite = fileToWrite + (serial + serialIncrement)
                        + ".txt";
                serialIncrement++;
                file = new File(root, fileToWrite);
            }

            FileWriter writer = new FileWriter(file);
            writer.append(fileContent);
            writer.flush();
            writer.close();
            Toast.makeText(getBaseContext(), "Saved " + file.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start, Stop, Read
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button:
                if(!reading) {
                    sensorInit();
                    Toast.makeText(getBaseContext(), "Started reading environment",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "You need to stop collecting data first",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button2:
                // stop
                if(reading) {
                    sensorKill();
                    sensortxt.setText("Sensors:");
                    Toast.makeText(getBaseContext(), "Stopped reading environment",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "You aren't collecting any data yet",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button3:
                boolean nothingToSave = fileContent.isEmpty();
                if(!reading && !nothingToSave) {
                    generateFile();
                    clearContent();
                } else if(nothingToSave) {
                    Toast.makeText(getBaseContext(), "There is no data to save",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "You need to stop collecting data first",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
