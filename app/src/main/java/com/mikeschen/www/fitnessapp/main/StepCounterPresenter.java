package com.mikeschen.www.fitnessapp.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.CursorIndexOutOfBoundsException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mikeschen.www.fitnessapp.Constants;
import com.mikeschen.www.fitnessapp.DatabaseHelper;
import com.mikeschen.www.fitnessapp.Steps;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class StepCounterPresenter implements
        StepCounterInterface.Presenter,
        SensorEventListener {

    private StepCounterInterface.View mStepCounterView;
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    private float totalAverageSpeed;
    private int speedCounted;
    private float grossTotalSpeed;
    private boolean checkSpeedDirection;

    private int caloriesBurned;

    private ArrayList<Float> speedData;

    private Timer timer;
    private TimerTask timerTask;

    private int currentStepsTableId;
    private Steps stepRecord;

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    DatabaseHelper db;

    private int fullDayInMillis = 86400000;

    public StepCounterPresenter(StepCounterInterface.View stepCounterInterface, Context context) {
        mStepCounterView = stepCounterInterface;
        mContext = context;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        long lastKnownTime = mSharedPreferences.getLong(Constants.PREFERENCES_TIME_KEY, 0);
        int lastKnownSteps = mSharedPreferences.getInt(Constants.PREFERENCES_STEPS_KEY, 0);
        long lastKnownId = mSharedPreferences.getLong(Constants.PREFERENCES_STEPS_ID_KEY, 0);
        Log.d("Last known steps", lastKnownSteps + "");
        Log.d("Last Known id", lastKnownId + "");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mEditor = mSharedPreferences.edit();

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//        stepCount = 0;
        totalAverageSpeed = 1;
        speedCounted = 1;
        grossTotalSpeed = 0;
        checkSpeedDirection = true;
        speedData = new ArrayList<>();
        caloriesBurned = 0;
        currentStepsTableId = 1;
        db = new DatabaseHelper(mContext.getApplicationContext());

        long currentTime = System.currentTimeMillis();
        long daysPassed;
        if(lastKnownTime > 0) {
            daysPassed = (currentTime / fullDayInMillis) - (lastKnownTime / fullDayInMillis);
        } else {
            daysPassed = 0;
        }
        Log.d("passed time", currentTime + "");
        Log.d("passed last known", lastKnownTime + "");
        Log.d("days Passed", daysPassed + "");

        if (daysPassed == 0) { //THIS IS FOR TURNING ON AND OFF WITHIN THE SAME DAY
            Log.d("database", "works");
            stepRecord = new Steps(lastKnownId, lastKnownSteps, 345);
        } else {
            stepRecord = new Steps(lastKnownId, 0, 345);
            if (daysPassed > 1) {
                for (int i = 0; i > daysPassed - 1; i++) { //FOR LOOP ADDS FIELDS FOR DAYS YOU MISSED
                    db.logSteps(stepRecord);
                }
            }
            long stepRecordId = db.logSteps(stepRecord); //FOR CURRENT DAY
            stepRecord.setId(stepRecordId);
        }

        stepRecord = new Steps(currentStepsTableId, lastKnownSteps, 345);

//        long stepRecord_id = db.logSteps(stepRecord);
//        stepRecord.setId(stepRecord_id);

//        Steps lastKnownSteps = db.getSteps(stepRecord.getId());
//        stepCount = lastKnownSteps.getStepsTaken();
//        db.updateSteps(stepRecord);
        db.closeDB();


        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("Does", "it work?");
                long currentTime = System.currentTimeMillis() / 1000;
                if (currentTime % (fullDayInMillis/60000) == 0) {

                    stepRecord = new Steps(currentStepsTableId, 0, 345);
                    long stepRecord_id = db.logSteps(stepRecord);
                    stepRecord.setId(stepRecord_id);
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 60000);

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(System.currentTimeMillis()-lastUpdate > 20) {

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                long curTime = System.currentTimeMillis();

                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 1000;
                if(speedData.size() < 20) {
                    speedData.add(speed);
                } else {
                    speedData.remove(0);
                    speedData.add(speed);
                }
                float localGrossSpeed = 0;
                float localAverageSpeed;
                for(int i = 0; i < speedData.size(); i++)  {
                    localGrossSpeed += speedData.get(i);
                }
                localAverageSpeed = localGrossSpeed/speedData.size();

                if(localAverageSpeed > 10) { // CHANGE THIS TO 20
                    speedCounted++;
                    grossTotalSpeed = grossTotalSpeed + speed;
                    totalAverageSpeed = (grossTotalSpeed) / speedCounted;
                }

                if(totalAverageSpeed > 15 && speedCounted > 200) {

                    if (checkSpeedDirection) {
                        if (localAverageSpeed > totalAverageSpeed) {
                            checkSpeedDirection = false;
                            stepRecord.setStepsTaken(stepRecord.getStepsTaken()+1);
                            loadSteps();
                        }
                    } else {
                        if (localAverageSpeed < totalAverageSpeed) {
                            checkSpeedDirection = true;
                            stepRecord.setStepsTaken(stepRecord.getStepsTaken()+1);
                            loadSteps();
                        }
                    }
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void loadSteps() {
        stepRecord = new Steps(stepRecord.getId(), stepRecord.getStepsTaken(), 345);
        db.updateSteps(stepRecord);
        mStepCounterView.showSteps(stepRecord.getStepsTaken());
        db.closeDB();
    }

    @Override
    public void loadCalories() {
        caloriesBurned = stepRecord.getStepsTaken() * 175/3500;
        mStepCounterView.showCalories(caloriesBurned);
    }

    public void onPause() {
        long destroyTime = System.currentTimeMillis();
        int destroySteps = stepRecord.getStepsTaken();
        long destroyId = stepRecord.getId();
        Log.d("Destroy Time", destroyTime + "");
        Log.d("Destroy Steps", destroySteps + "");
        addToSharedPreferences(destroyTime, destroySteps, destroyId);
        Log.d("Shared Prefs", mSharedPreferences + "");
    }

    private void addToSharedPreferences(long time, int steps, long id) {
        mEditor.putLong(Constants.PREFERENCES_TIME_KEY, time).apply();
        mEditor.putInt(Constants.PREFERENCES_STEPS_KEY, steps).apply();
        mEditor.putLong(Constants.PREFERENCES_STEPS_ID_KEY, id).apply();
    }
}

