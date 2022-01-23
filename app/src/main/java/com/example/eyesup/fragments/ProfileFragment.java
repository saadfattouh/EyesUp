package com.example.eyesup.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.ConditionVariable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.eyesup.R;
import com.example.eyesup.helper.SharedPrefManager;
import com.example.eyesup.model.User;


public class ProfileFragment extends Fragment {

    public static final String TAG = "profileFragment";

    Button mLogoutBtn;

    TextView mUserNameTV;
    TextView mPhoneTV;
    TextView mAddressTV;


    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mLogoutBtn = view.findViewById(R.id.logout);

        mUserNameTV = view.findViewById(R.id.user_name_text_view);
        mPhoneTV = view.findViewById(R.id.phone_text_view);
        mAddressTV = view.findViewById(R.id.address_text_view);

        User user = SharedPrefManager.getInstance(requireContext()).getUserDate();

        mUserNameTV.setText(user.getUserName());
        mPhoneTV.setText(user.getPhone());
        mAddressTV.setText(user.getAddress());

        mLogoutBtn.setOnClickListener(v -> {
            logOut();
        });

    }

    public void logOut(){
        Context ctx = requireContext();
        SharedPrefManager.getInstance(ctx).logout();
        PackageManager packageManager = ctx.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(ctx.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }
}