package edu.unc.sjyan.beanlog;

// Android Packages
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

// Bean Packages
import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.BeanManager;
import com.punchthrough.bean.sdk.message.Acceleration;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.Callback;
import com.punchthrough.bean.sdk.message.ScratchBank;

// JAVA-ML Packages
import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.EuclideanDistance;
import net.sf.javaml.tools.data.FileHandler;

// Std Java Lib
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity  {

    Bean b;
    Thread accThread;
    Thread predictThread;
    boolean beanDiscovered;
    boolean reading;
    boolean classifying;
    static String fileContent = "";
    static String activity = "";
    static String location = "";
    static int serial = 1;
    TextView classification;
    List<Double> currentData = new ArrayList<>();
    Classifier knn;

    public enum Classification {STANDING, SITTING, WALKING, RUNNING, UPSTAIRS, DOWNSTAIRS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BeanManager.getInstance().startDiscovery(bdl);

        classification = (TextView) findViewById(R.id.textView4);
        beanDiscovered = false;
        train();
    }

    BeanDiscoveryListener bdl = new BeanDiscoveryListener() {
        @Override
        public void onBeanDiscovered(Bean bean, int rssi) {
            Log.v("TT", "" + bean.getDevice() + ", " + rssi);
            b = bean;
        }

        @Override
        public void onDiscoveryComplete() {
            b.connect(getApplicationContext(), blsnr);
        }
    };

    BeanListener blsnr = new BeanListener() {
        @Override
        public void onConnected() {
            beanDiscovered = true;

            Log.v("TT", "We are connected to: " + b.getDevice().getName());
            Toast.makeText(getBaseContext(), "Connected to " + b.getDevice().getName(),
                    Toast.LENGTH_LONG).show();

            b.readAcceleration(new Callback<Acceleration>() {
                @Override
                public void onResult(Acceleration result) {
                    Log.v("TT", "" + result.x() + ", " + result.y() + ", " + result.z());
                }
            });
        }

        @Override
        public void onConnectionFailed() {
            Toast.makeText(getBaseContext(), "Bean failed to connect",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDisconnected() {
            Toast.makeText(getBaseContext(), "Oh no! Bean disconnected!",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSerialMessageReceived(byte[] data) {

        }

        @Override
        public void onScratchValueChanged(ScratchBank bank, byte[] value) {

        }

        @Override
        public void onError(BeanError error) {
            Toast.makeText(getBaseContext(), "Bean suffered an unexpected error",
                    Toast.LENGTH_LONG).show();
        }
    };

    public void getAcceleration() {
        reading = true;
        accThread = new Thread(new Runnable() {
            public void run() {
                // check if being read
                while(!Thread.currentThread().isInterrupted() && reading) {
                    b.readAcceleration(new Callback<Acceleration>() {
                        @Override
                        public void onResult(Acceleration result) {
                            appendContent(System.currentTimeMillis(), result.x(), result.y(),
                                    result.z());
                        }
                    });

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void classifyActivity() {
        classifying = true;
        predictThread = new Thread(new Runnable() {
            public void run() {
                // check if being read
                while(!Thread.currentThread().isInterrupted() && classifying) {
                    b.readAcceleration(new Callback<Acceleration>() {
                        @Override
                        public void onResult(Acceleration result) {
                            handleClassification(result.x(), result.y(), result.z());
                        }
                    });

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void clearContent() {
        fileContent = "";
    }

    public void appendContent(long timestamp, double aX, double aY, double aZ) {
        // Timestamp timestamp = new Timestamp(time);
        fileContent += timestamp + "\t" + aX + "\t" + aY + "\t" + aZ + "\n";
        Log.v("TT", "" + timestamp + aX + ", "
                + aY + ", " + aZ);
    }

    public void generateFile() {
        reading = false;
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "bean_data_sjyan");
            if (!root.exists()) {
                root.mkdirs();
            }

            String fileToWrite = activity + "_" + location + "_" + serial + ".txt";
            File file = new File(root, fileToWrite);

            int serialIncrement = 1;
            while(file.exists()) {
                fileToWrite = activity + "_" + location + "_" + (serial + serialIncrement)
                        + ".txt";
                serialIncrement++;
                file = new File(root, fileToWrite);
            }

            FileWriter writer = new FileWriter(file);
            writer.append(fileContent);
            writer.flush();
            writer.close();
            Toast.makeText(getBaseContext(), "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Choose activity
    public void onRadioGroup1Clicked(View v) {
        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();

        // Check which radio button was clicked
        switch(v.getId()) {
            case R.id.radioButton:
                if (checked)
                    // Standing
                    activity = "standing";
                    break;
            case R.id.radioButton2:
                if (checked)
                    // Sitting
                    activity = "sitting";
                    break;
            case R.id.radioButton3:
                if(checked)
                    // Walking
                    activity = "walking";
                    break;
            case R.id.radioButton4:
                if(checked)
                    // Running
                    activity = "running";
                    break;
            case R.id.radioButton5:
                if(checked)
                    // Upstairs
                    activity = "upstairs";
                    break;
            case R.id.radioButton6:
                if(checked)
                    // Downstairs
                    activity = "downstairs";
                    break;
            default:
                // alert to select activity
                break;
        }
    }

    // Choose wearable location
    public void onRadioGroup2Clicked(View v) {
        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();

        // Check which radio button was clicked
        switch(v.getId()) {
            case R.id.radioButton7:
                if (checked)
                    // Waist
                    location = "waist";
                    break;
            case R.id.radioButton8:
                if (checked)
                    // Wrist
                    location = "wrist";
                    break;
            case R.id.radioButton9:
                if(checked)
                    // Shoe
                    location = "shoe";
                    break;
            default:
                // alert to select wearable location
                break;
        }
    }

    // Start, Stop, Read
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button:
                // start
                // get acceleration data every 10th of a second
                if(!activity.equals("") && !location.equals("") && beanDiscovered) {
                    getAcceleration();
                    accThread.start();
                    Toast.makeText(getBaseContext(), "Started reading acceleration",
                            Toast.LENGTH_SHORT).show();
                } else if(activity.equals("") && beanDiscovered) {
                    Toast.makeText(getBaseContext(), "Please select an activity",
                            Toast.LENGTH_SHORT).show();
                } else if(location.equals("") && beanDiscovered) {
                    Toast.makeText(getBaseContext(), "Please select a location",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Bean is not connected",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button2:
                // stop
                if(beanDiscovered) {
                    reading = false;
                    Toast.makeText(getBaseContext(), "Stopped reading acceleration",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Bean is not connected",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button3:
                // save
                // format into file accelerometer values
                if(!reading && beanDiscovered) {
                    generateFile();
                    clearContent();
                } else if(beanDiscovered) {
                    Toast.makeText(getBaseContext(), "You must STOP reading data first",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "Bean is not connected",
                            Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.button4:
                // start current activity classification
                if(beanDiscovered) {
                    classifyActivity();
                    predictThread.start();
                } else {
                    Toast.makeText(getBaseContext(), "Bean is not connected",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button5:
                // stop classification
                if(beanDiscovered) {
                    classifying = false;
                    classification.setText("");
                } else {
                    Toast.makeText(getBaseContext(), "Bean is not connected",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    public double[] toArray(String path) {
        String nextValue = "";
        double[] accelerations = new double[100];
        try {
            File file = new File(Environment.getExternalStorageDirectory() +
                    "/bean_data_sjyan/" + path);

            Scanner toRead = new Scanner(new File(file.getPath()));

            int i = 0;
            while(toRead.hasNext() && (i < accelerations.length)) {
                nextValue = toRead.useDelimiter("\n").next();
                accelerations[i] = Double.parseDouble(nextValue);
                i++;
            }

            return accelerations;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void train() {
        Dataset trainingSet = new DefaultDataset();
        File folder = new File(Environment.getExternalStorageDirectory() + "/bean_data_sjyan");

        for(File f : folder.listFiles()) {
            if(f.getName().startsWith("t_")) {
                Instance ins;
                if(f.getName().contains("standing")) {
                    ins = new DenseInstance(toArray(f.getName()), Classification.STANDING);
                    trainingSet.add(ins);
                } else if(f.getName().contains("sitting")) {
                    ins = new DenseInstance(toArray(f.getName()), Classification.SITTING);
                    trainingSet.add(ins);
                } else if(f.getName().contains("walking")) {
                    ins = new DenseInstance(toArray(f.getName()), Classification.WALKING);
                    trainingSet.add(ins);
                } else if(f.getName().contains("running")) {
                    ins = new DenseInstance(toArray(f.getName()), Classification.RUNNING);
                    trainingSet.add(ins);
                } else if(f.getName().contains("upstairs")) {
                    ins = new DenseInstance(toArray(f.getName()), Classification.UPSTAIRS);
                    trainingSet.add(ins);
                } else if(f.getName().contains("downstairs")) {
                    ins = new DenseInstance(toArray(f.getName()), Classification.DOWNSTAIRS);
                    trainingSet.add(ins);
                }
            }
        }

        knn = new KNearestNeighbors(10);
        knn.buildClassifier(trainingSet);
    }

    public double overallAccel(double aX, double aY, double aZ) {
        return Math.sqrt(aX * aX + aY * aY + aZ * aZ);
    }

    public double[] generatePrimitive(List<Double> data) {
        double [] primitive = new double[10];
        for(int i = 0; i < data.size(); i++) {
            primitive[i] = data.get(i);
        }

        return primitive;
    }

    public void handleClassification(double aX, double aY, double aZ) {

        currentData.add(overallAccel(aX, aY, aZ));
        Log.d("TT", "" + overallAccel(aX, aY, aZ));


        if(currentData.size() == 10) {
            Instance liveInstance = new DenseInstance(generatePrimitive(currentData));
            Object prediction = knn.classify(liveInstance);
            Log.d("TT", prediction.toString());
            classification.setTextColor(Color.RED);
            classification.setText(prediction.toString());

            // reset
            currentData.clear();
        }

    }


}
