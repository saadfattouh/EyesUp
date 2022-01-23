package com.example.eyesup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.eyesup.fragments.DrowsinessDetectionFragment;
import com.example.eyesup.fragments.MainFragment;
import com.example.eyesup.fragments.MapFragment;
import com.example.eyesup.fragments.OptionsFragment;
import com.example.eyesup.fragments.ProfileFragment;
import com.example.eyesup.helper.PermissionsChecker;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {


    private final int PERMISSIONS_REQUEST_CODE = 500;

    TextView mTitle;
    BottomNavigationView mNavigationBar;

    DrowsinessDetectionFragment mDrowsinessDetectionFragment;
    MainFragment mMainFragment;
    MapFragment mMapFragment;
    OptionsFragment mOptionsFragment;
    ProfileFragment mProfileFragment;

    FragmentManager fm;

    boolean outOfMainFragment = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationBar = findViewById(R.id.bottom_nav_bar);

        mTitle = findViewById(R.id.title);

        mMainFragment = new MainFragment();
        mProfileFragment = new ProfileFragment();
        mDrowsinessDetectionFragment = new DrowsinessDetectionFragment();
        mMapFragment = new MapFragment();
        mOptionsFragment = new OptionsFragment();

        fm = getSupportFragmentManager();

        if(savedInstanceState == null){
            //setting the first destination
            fm.beginTransaction().add(R.id.fragment_container, mMainFragment, MainFragment.TAG).commit();
            mNavigationBar.setSelectedItemId(R.id.home);
            mTitle.setVisibility(View.VISIBLE);
        }



        //setting up the bottom navigation listener for navigating events
        mNavigationBar.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.cafe_location:
                    outOfMainFragment = true;
                    fm.beginTransaction().replace(R.id.fragment_container, mMapFragment, MapFragment.TAG).commit();
                    mTitle.setVisibility(View.GONE);
                    return true;
                case R.id.options:
                    outOfMainFragment = true;
                    fm.beginTransaction().replace(R.id.fragment_container, mOptionsFragment, OptionsFragment.TAG).commit();
                    mTitle.setText("Options");
                    mTitle.setVisibility(View.VISIBLE);
                    return true;
                case R.id.home:
                    outOfMainFragment = false;
                    fm.beginTransaction().replace(R.id.fragment_container, mMainFragment, MainFragment.TAG).commit();
                    mTitle.setText("Todo list");
                    mTitle.setVisibility(View.VISIBLE);
                    return true;
                case R.id.account:
                    outOfMainFragment = true;
                    fm.beginTransaction().replace(R.id.fragment_container, mProfileFragment, ProfileFragment.TAG).commit();
                    mTitle.setText("Profile");
                    mTitle.setVisibility(View.VISIBLE);
                    return true;
                case R.id.detection:
                    outOfMainFragment = true;
                    fm.beginTransaction().replace(R.id.fragment_container, mDrowsinessDetectionFragment, DrowsinessDetectionFragment.TAG).commit();
                    mTitle.setText("detecting...");
                    mTitle.setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        });


        String[] permissions = {Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CALL_PHONE};

        PermissionsChecker.firstTimeRequestPermissions(this, this, permissions);


    }


    @Override
    public void onBackPressed() {
        if(outOfMainFragment){
            outOfMainFragment = false;
            fm.beginTransaction().replace(R.id.fragment_container, mMainFragment, MainFragment.TAG).commit();
            mTitle.setVisibility(View.VISIBLE);
        }else {
            finish();
        }
    }
}

