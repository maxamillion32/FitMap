package com.mikeschen.www.fitnessapp;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        MainInterface.View,
        MapInterface.View,
        StepCounterInterface.View,
        View.OnClickListener {

    private boolean mPermissionDenied = false;
    private int caloriesBurned = 0;
    private String buttonDisplay;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    private Context mContext;
    private TipPresenter mTipPresenter;
    private MapPresenter mMapPresenter;
    private StepCounterPresenter mStepCounterPresenter;
    private Steps stepRecord;

    @Bind(R.id.mainButton) Button mMainButton;
    @Bind(R.id.tipTextView) TextView mTipTextView;
    @Bind(R.id.tipsTextView) TextView mTipsTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        Typeface myTypeFace = Typeface.createFromAsset(getAssets(), "fonts/PTN77F.ttf");
        mMainButton.setTypeface(myTypeFace);
        mTipsTextView.setTypeface(myTypeFace);
        mTipTextView.setTypeface(myTypeFace);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        buttonDisplay = "Calories";
        mMainButton.setText("Calories Burned: " + caloriesBurned);
        mMainButton.setOnClickListener(this);
        mContext =  this;

        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        mTipPresenter = new TipPresenter(this, mContext);
        mMapPresenter = new MapPresenter(this, mContext, mapFragment);
        mStepCounterPresenter = new StepCounterPresenter(this, mContext);
        stepRecord = new Steps();

        addDrawerItems();
        setupDrawer();
        mTipPresenter.loadTip();
        mMapPresenter.loadMap();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
            Log.d("onResumeFragments", "Here?");
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
        Log.d("UpdatePermissionStatus", "Hello");
        mPermissionDenied = permissionStatus;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case(R.id.mainButton) :
                if(buttonDisplay.equals("Calories")) {
                    buttonDisplay = "Steps";
                    mStepCounterPresenter.loadSteps();
                } else if(buttonDisplay.equals("Steps")) {
                    buttonDisplay = "Calories";
                    mStepCounterPresenter.loadCalories();
                }
        }
    }

    private void addDrawerItems() {
        String[] navArray = {"Main", "Maps", "Meals", "Stats", "About"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, navArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
    return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void showMap() {

    }

    @Override
    public void showSteps(int stepCount) {
        mMainButton.setText("Steps Taken: " + stepCount);
    }

    @Override
    public void showCalories(int caloriesBurned) {
        mMainButton.setText("Calories Burned: " + caloriesBurned);
    }
}






