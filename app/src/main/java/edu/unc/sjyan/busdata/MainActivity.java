package edu.unc.sjyan.busdata;

// Android Packages
import android.app.Dialog;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

// Std Java Lib
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// Google play services
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener, ResultCallback<Status>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    boolean reading;
    static String fileContent = "";
    static int serial = 1;
    TextView sensortxt;
    TextView busStop;
    SeekBar stopSlider;
    Switch gpsSwitch;
    private long lastUpdate = 0;
    SensorManager sensorManager;
    Sensor tempSensor, humidSensor, acceleroSensor,linAcceleroSensor, magnetSensor, gyroscopeSensor,
            barometerSensor, lightSensor;
    private GoogleApiClient c = null;
    TextView infoText;
    TextView currentBusStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buildGoogleApiClient();
        infoText = (TextView) findViewById(R.id.infoText);
        currentBusStop = (TextView) findViewById(R.id.currentBusStop);
        sensortxt = (TextView) findViewById(R.id.textViewSensors);
        busStop = (TextView) findViewById(R.id.busStopText);
        stopSlider = (SeekBar) findViewById(R.id.busStopSlider);
        gpsSwitch = (Switch) findViewById(R.id.switch1);

        stopSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                //.setTextSize(progress);
                busStop.setText("Bus Stop:" + progress);
                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("TAG", "We are connected to Google Services");
        Toast.makeText(getBaseContext(), "Connected to GPS",
                Toast.LENGTH_SHORT).show();
        try {
            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(500);
            mLocationRequest.setFastestInterval(250);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(c, mLocationRequest, this);

            Location loc = LocationServices.FusedLocationApi.getLastLocation(c);
            if(loc == null) {
                Toast.makeText(getBaseContext(), "Provider is null - GPS won't log",
                        Toast.LENGTH_SHORT).show();
            }
            Constants.latString = (loc != null) ? "" + loc.getLatitude() : "-9999";
            Constants.longString = (loc != null) ? "" + loc.getLongitude() : "-9999";
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        c = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("Connection" , " suspended");
        Toast.makeText(getBaseContext(), "GPS Suspended",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("Connection Status", connectionResult.toString());
        Toast.makeText(getBaseContext(), "GPS Connection Failed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        // Pause after build - connect only when reading data
        // c.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        c.disconnect();
        super.onStop();
    }


    @Override
    public void onResult(Status status) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Constants.latString = "" + location.getLatitude();
        Constants.longString = "" + location.getLongitude();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            busStop.setText("Bus stop: " + stopSlider.getProgress());
            busStop.invalidate();

            int currType = event.sensor.getType();

            if(currType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                Constants.temperature = "" + event.values[0];
            }
            else if(currType == Sensor.TYPE_RELATIVE_HUMIDITY) {
                Constants.humidity = "" + event.values[0];
            }
            else if(currType == Sensor.TYPE_ACCELEROMETER) {
                Constants.acceloString = event.values[0] + "," + event.values[1] + "," +
                        event.values[2];
            }
            else if(currType == Sensor.TYPE_LINEAR_ACCELERATION){
                Constants.linAcceloString = event.values[0] + "," + event.values[1] + "," +
                        event.values[2];
            }
            else if(currType == Sensor.TYPE_MAGNETIC_FIELD) {
                Constants.magnet = event.values[0] + "," + event.values[1] + "," +
                        event.values[2];
            }
            else if(currType == Sensor.TYPE_GYROSCOPE) {
                Constants.gyroString = event.values[0] + "," + event.values[1] + "," +
                        event.values[2];
            }
            else if(currType == Sensor.TYPE_PRESSURE) {
                Constants.baroString = "" + event.values[0];
            }
            else if(currType == Sensor.TYPE_LIGHT) {
                Constants.lightString = "" + event.values[0];
            }

            /*We update the list of sensor values once every second*/
            if(System.currentTimeMillis() - lastUpdate >= 100) {

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                String currentDateandTime = sdf.format(new Date());
                long milliseconds = System.currentTimeMillis();
                String str = /*currentDateandTime*/milliseconds + "," + Constants.temperature + "," +
                        Constants.humidity + "," + Constants.baroString + "," +
                        Constants.acceloString + "," + Constants.linAcceloString + "," +  Constants.magnet +
                        "," + Constants.gyroString + "," + Constants.lightString + "," +
                        Constants.latString + "," + Constants.longString + "," +
                        Constants.stopString;

                Constants.ALL_SENSOR_STR = str;
                appendContent(Constants.ALL_SENSOR_STR + "\n");
                //Constants.stopString = "-9999"; // reset bus stop
                //Constants.list.add(str);
                lastUpdate = System.currentTimeMillis();
                infoText.setText(currentDateandTime);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void sensorKill() {
        busStop.setText("Bus stop:");
        sensorManager.unregisterListener(this);
        reading = false;
    }

    public void sensorInit() {
        reading = true;
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);

        tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        linAcceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        humidSensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(int i = 0 ; i < sensorList.size(); i++){
            Log.v("SENSOR LIST", sensorList.get(i).toString());
        }


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

        if(linAcceleroSensor!=null) {
            sensorManager.registerListener(this, linAcceleroSensor, 900);
            senlist += " LINACC ";
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
            //String filePath = Environment.getExternalStorageState().toString()+"test.txt";


            //String youFilePath = Environment.getExternalStorageDirectory().toString()+"/"+today+"_"+Constants.USER;
            //String youFilePath = Environment.getExternalStorageDirectory().toString();
            File root = new File(Environment.getExternalStorageDirectory().toString(), today + "_" + Constants.USER);
            //File root = new File(youFilePath);
            if (!root.exists()) {
                root.mkdirs();
            }

            sdf = new SimpleDateFormat("HHmmss");
            String fileToWrite = sdf.format(new Date()) + ".csv";
            File file = new File(root, fileToWrite);

            // In rare case of same time log - should probably handle this elsewhere
            /*int serialIncrement = 1;
            while(file.exists()) {
                fileToWrite = fileToWrite + (serial + serialIncrement)
                        + ".txt";
                serialIncrement++;
                file = new File(root, fileToWrite);
            }*/

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
            case R.id.startButton:
                if(!reading) {
                    if(gpsSwitch.isChecked()) { c.connect(); }
                    sensorInit();
                    infoText.setTextColor(Color.parseColor("#45ef6d"));
                    Toast.makeText(getBaseContext(), "Started reading environment",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "You need to stop collecting data first",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stopButton:
                // stop
                if(reading) {
                    infoText.setTextColor(Color.parseColor("#ff4040"));
                    c.disconnect();
                    sensorKill();
                    sensortxt.setText("Sensors:");
                    Toast.makeText(getBaseContext(), "Stopped reading environment",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "You aren't collecting any data yet",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.saveButton:
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
            case R.id.busStopStartButton:
                if(reading) {
                    Constants.stopString = "" + stopSlider.getProgress();
                    currentBusStop.setText("At Bus Stop: " + stopSlider.getProgress());
                    Toast.makeText(getBaseContext(), "Start bus stop " + stopSlider.getProgress(),
                            Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getBaseContext(), "You need to start collecting data before you can log bus stops",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.busStopEndButton:
                if(reading) {
                    Constants.stopString = "-9999";
                    currentBusStop.setText("At Bus Stop: NA");
                    Toast.makeText(getBaseContext(), "Stop bus stop " + stopSlider.getProgress(),
                            Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getBaseContext(), "You need to start collecting data before you can log bus stops",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
}
