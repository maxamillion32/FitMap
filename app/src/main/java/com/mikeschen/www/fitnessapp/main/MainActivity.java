package com.mikeschen.www.fitnessapp.main;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.mikeschen.www.fitnessapp.BaseActivity;
import com.mikeschen.www.fitnessapp.R;
import com.mikeschen.www.fitnessapp.maps.MapInterface;
import com.mikeschen.www.fitnessapp.maps.MapPresenter;
import com.mikeschen.www.fitnessapp.maps.MapsActivity;
import com.mikeschen.www.fitnessapp.utils.PermissionUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends BaseActivity implements
        MainInterface.View,
        MapInterface.View,
        StepCounterInterface.View,
        View.OnClickListener {

    private boolean mPermissionDenied = false;
    private int caloriesBurned = 0;
    private String buttonDisplay;

    private Context mContext;
    private TipPresenter mTipPresenter;
    private MapPresenter mMapPresenter;
    private StepCounterPresenter mStepCounterPresenter;

//    private SharedPreferences mSharedPreferences;
//    private SharedPreferences.Editor mEditor;

    @Bind(R.id.mainButton) Button mMainButton;
    @Bind(R.id.tipTextView) TextView mTipTextView;
    @Bind(R.id.tipsTextView) TextView mTipsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        long lastKnownTime = mSharedPreferences.getLong(Constants.PREFERENCES_TIME_KEY, 0);
//        int lastKnownSteps = mSharedPreferences.getInt(Constants.PREFERENCES_STEPS_KEY, 1);
//        Log.d("Last known steps", lastKnownSteps + "");

//        previous font; may not need it anymore; calligraphy can change fonts to any files we want


//        Typeface myTypeFace = Typeface.createFromAsset(getAssets(), "fonts/PTN77F.ttf");
//        mMainButton.setTypeface(myTypeFace);
//        mTipsTextView.setTypeface(myTypeFace);
//        mTipTextView.setTypeface(myTypeFace);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        buttonDisplay = "Calories";
        buttonDisplay = "Calories";
        mMainButton.setText("Calories Burned: " + caloriesBurned);
        mMainButton.setOnClickListener(this);
        mContext = this;

//        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        mEditor = mSharedPreferences.edit();

//
//        mDrawerList = (ListView) findViewById(R.id.navList);
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mActivityTitle = getTitle().toString();

        mTipPresenter = new TipPresenter(this, mContext);
        mMapPresenter = new MapPresenter(this, mContext, mapFragment);
        mStepCounterPresenter = new StepCounterPresenter(this, mContext);

        mTipPresenter.loadTip();
        mMapPresenter.loadMap();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);



        mStepCounterPresenter.loadSteps();//This sets text in Steps Taken Button on start
    }


    //Calligraphy

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        ButterKnife.bind(this);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide action item
                getSupportActionBar().setTitle("");
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                getSupportActionBar().setTitle("FitnessApp");
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String destination) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("destination", destination);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }


    @Override
    public void showTip(String tip) {
        mTipTextView.setText(tip);
    }

    //Google Maps
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void updatePermissionStatus(boolean permissionStatus) {
        mPermissionDenied = permissionStatus;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.mainButton):
                if (buttonDisplay.equals("Calories")) {
                    buttonDisplay = "Steps";
                    mStepCounterPresenter.loadSteps();
                } else if (buttonDisplay.equals("Steps")) {
                    buttonDisplay = "Calories";
                    mStepCounterPresenter.loadCalories();
                }
        }
    }

    @Override
    public void showMap() {
    }

    @Override
    public void showDistance(String distance) {
    }

    @Override
    public void showDuration(String duration) {
    }

    @Override
    public void showCalorieRoute(int calorie) {
    }

    @Override
    public void showSteps(int stepCount) {
        mMainButton.setText("Steps Taken: " + stepCount);
    }

    @Override
    public void showCalories(int caloriesBurned) {
        mMainButton.setText("Calories Burned: " + caloriesBurned);
    }

    @Override
    public void onPause() {
//        long destroyTime = System.currentTimeMillis() / 1000;
//        int destroySteps = mStepCounterPresenter.getStepCount();
//        int destroyId = mStepCounterPresenter.
//
//        Log.d("Destroy Time", destroyTime + "");
//        Log.d("Destroy Steps", destroySteps + "");
//        addToSharedPreferences(destroyTime, destroySteps);
//        Log.d("Shared Prefs", mSharedPreferences + "");
        mStepCounterPresenter.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

//    private void addToSharedPreferences(long time, int steps) {
//        mEditor.putLong(Constants.PREFERENCES_TIME_KEY, time).apply();
//        mEditor.putInt(Constants.PREFERENCES_STEPS_KEY, steps).apply();
//    }

    public void refresh() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}

