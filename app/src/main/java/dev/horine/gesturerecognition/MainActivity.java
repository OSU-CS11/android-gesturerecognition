package dev.horine.gesturerecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gravity;
    private Sensor gyroscope;
    public Vibrator v;
    private TextView chopDisplay;
    private float vibrateThreshold = 2;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float gravityX = 0;
    private float gravityY = 0;
    private float gravityZ = 0;

    private float gyroX = 0;
    private float gyroY = 0;
    private float gyroZ = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private float lastX, lastY, lastZ;

    boolean up, down, left, right, forward, back;

    boolean recording = false;
    boolean masterRecord = false;
    Vector<float[]> values = new Vector<float[]>();

    private TextView currentX, currentY, currentZ;
    private TextView gravityXview, gravityYview, gravityZview;
    private TextView gyroXview, gyroYview, gyroZview;
    private ImageView upArrow, downArrow, leftArrow, rightArrow, forwardArrow, backArrow;
    private Button masterStart, masterStop, exportButton, clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
                && sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            // success! we have sensors!

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange()/2;
            v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        } else {
            // fail! we dont have an accelerometer!
        }


        setContentView(R.layout.activity_main);
        // = findViewById(R.id.chopDisplay);
        currentX = findViewById(R.id.currentX);
        currentY = findViewById(R.id.currentY);
        currentZ = findViewById(R.id.currentZ);

        gravityXview = findViewById(R.id.gravityX);
        gravityYview = findViewById(R.id.gravityY);
        gravityZview = findViewById(R.id.gravityZ);

        leftArrow = findViewById(R.id.leftArrow);
        rightArrow = findViewById(R.id.rightArrow);
        upArrow = findViewById(R.id.upArrow);
        downArrow = findViewById(R.id.downArrow);
        forwardArrow = findViewById(R.id.forwardArrow);
        backArrow = findViewById(R.id.backArrow);

        masterStart = findViewById(R.id.masterStart);
        masterStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exportButton.setEnabled(false);
                clearButton.setEnabled(true);
                masterStop.setEnabled(true);
                masterStart.setEnabled(false);
                masterRecord = true;
                recording = true;
                //values.clear();
            }
        });

        masterStop = findViewById(R.id.masterStop);
        masterStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                values.add(new float[]{-10000, -10000, -10000});
                exportButton.setEnabled(true);
                masterStop.setEnabled(false);
                masterStart.setEnabled(true);
                masterRecord = false;
                recording = false;
            }
        });


        exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String filename = System.currentTimeMillis()+".csv";
                try {
                    FileWriter fw = new FileWriter(new File(getExternalFilesDir(null), filename));
                    fw.append("Accelerometer X, Accelerometer Y, Accelerometer Z, Gravity X, Gravity Y, Gravity Z, Gyro X, Gyro Y, Gyro Z\n");
                    for(int i = 0; i < values.size(); i++) {
                        fw.append(values.get(i)[0]+","+values.get(i)[1]+","+values.get(i)[2]+",");
                        fw.append(values.get(i)[3]+","+values.get(i)[4]+","+values.get(i)[5]+",");
                        fw.append(values.get(i)[6]+","+values.get(i)[7]+","+values.get(i)[8]+"\n");
                    }
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                android.widget.Toast.makeText(getApplicationContext(),"dev.horine.gesturerecognition/files/" + filename,Toast.LENGTH_LONG).show();
                values.clear();
                exportButton.setEnabled(false);
                clearButton.setEnabled(true);
            }
        });

        clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                values.clear();
                masterStop.setEnabled(false);
                masterStart.setEnabled(true);
                masterRecord = false;
                recording = false;
                exportButton.setEnabled(false);
                clearButton.setEnabled(false);
            }
        });


    }

    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == gravity) {
            gravityX = event.values[0];
            gravityY = event.values[1];
            gravityZ = event.values[2];

            gravityXview.setText(Float.toString(gravityX));
            gravityYview.setText(Float.toString(gravityY));
            gravityZview.setText(Float.toString(gravityZ));
        } else if (event.sensor == gyroscope) {
            gyroX = event.values[0];
            gyroY = event.values[1];
            gyroZ = event.values[2];

            gyroXview.setText(Float.toString(gyroX));
            gyroYview.setText(Float.toString(gyroY));
            gyroZview.setText(Float.toString(gyroZ));
        } else {
            // clean current values
            displayCleanValues();
            // display the current x,y,z accelerometer values
            displayCurrentValues();
            // display the max x,y,z accelerometer values
            //displayMaxValues();

            // get the change of the x,y,z values of the accelerometer
            float offset = -9.8f;
            if (event.values[2] < 0) {
                offset = 9.8f;
            }

            deltaX = lastX - (event.values[0] - gravityX);
            deltaY = lastY - (event.values[1] - gravityY);
            deltaZ = lastZ - (event.values[2] - gravityZ);

            lastX = event.values[0] - gravityX;
            lastY = event.values[1] - gravityY;
            lastZ = event.values[2] - gravityZ;

            float noiseFloor = 1;
            // if the change is below noiseFloor, it is just plain noise
            if (deltaX < noiseFloor && deltaX > -noiseFloor) {
                deltaX = 0;
                right = false;
                left = false;
            }
            if (deltaZ < noiseFloor && deltaZ > -noiseFloor) {
                deltaZ = 0;
                up = false;
                down = false;
            }
            if (deltaY < noiseFloor && deltaY > -noiseFloor) {
                deltaY = 0;
                forward = false;
                back = false;
            }

            if (!masterRecord && false) {
                if (deltaX == 0 && deltaY == 0 && deltaZ == 0) {
                    if (recording) { //stopRecording
                        recording = false;
                        values.add(new float[]{-10000, -10000, -10000});
                        exportButton.setEnabled(true);
                        clearButton.setEnabled(true);
                    }
                } else {
                    if (!recording) { //startRecording
                        recording = true;
                    }
                }
            }

            if (recording) {
                values.add(new float[]{event.values[0] - gravityX, lastY = event.values[1] - gravityY, lastZ = event.values[2] - gravityZ,
                                       gravityX, gravityY, gravityZ,
                                       gyroX, gyroY, gyroZ});
            }

            if (deltaX > vibrateThreshold) {
                v.vibrate(50);
                right = true;
            }
            if (deltaX < -vibrateThreshold) {
                v.vibrate(50);
                left = true;
            }
            if (deltaZ > vibrateThreshold) {
                v.vibrate(50);
                down = true;
            }
            if (deltaZ < -vibrateThreshold) {
                v.vibrate(50);
                up = true;
            }
            if (deltaY > vibrateThreshold) {
                v.vibrate(50);
                forward = true;
            }
            if (deltaY < -vibrateThreshold) {
                v.vibrate(50);
                back = true;
            }
        }
    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
        leftArrow.setVisibility(View.INVISIBLE);
        rightArrow.setVisibility(View.INVISIBLE);
        upArrow.setVisibility(View.INVISIBLE);
        downArrow.setVisibility(View.INVISIBLE);
        forwardArrow.setVisibility(View.INVISIBLE);
        backArrow.setVisibility(View.INVISIBLE);

    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));


        if (left) {leftArrow.setVisibility(View.VISIBLE);}
        if (right) {rightArrow.setVisibility(View.VISIBLE);}
        if (up) {upArrow.setVisibility(View.VISIBLE);}
        if (down) {downArrow.setVisibility(View.VISIBLE);}
        if (forward) {forwardArrow.setVisibility(View.VISIBLE);}
        if (back) {backArrow.setVisibility(View.VISIBLE);}

    }
}
