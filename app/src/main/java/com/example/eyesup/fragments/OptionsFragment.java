package com.example.eyesup.fragments;

import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.eyesup.R;
import com.example.eyesup.helper.SharedPrefManager;


public class OptionsFragment extends Fragment {

    public static final String TAG = "optionsFragment";

    CheckBox mEnableEmergencyCalls;
    RelativeLayout mContactOption;
    EditText mContactNumberET;
    Button mSavePhoneBtn;

    RadioGroup mAlarmTypeChooser;
    RelativeLayout mTTSLayout;
    EditText mTTSMessage;
    Button mSaveMessageBtn;

    Context ctx;
    SharedPrefManager prefManager;



    public OptionsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ctx = requireContext();
        prefManager = SharedPrefManager.getInstance(ctx);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEnableEmergencyCalls = view.findViewById(R.id.enable_calls);
        mContactOption = view.findViewById(R.id.contact_layout);
        mContactNumberET = view.findViewById(R.id.phone);
        mSavePhoneBtn = view.findViewById(R.id.save_phone);
        mAlarmTypeChooser = view.findViewById(R.id.alarm_chooser);
        mTTSLayout = view.findViewById(R.id.tts_layout);
        mTTSMessage = view.findViewById(R.id.message);
        mSaveMessageBtn = view.findViewById(R.id.save_message);


        mEnableEmergencyCalls.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                mContactOption.setVisibility(View.VISIBLE);
                mSavePhoneBtn.setOnClickListener(v -> {
                    String phoneNumber = mContactNumberET.getText().toString();
                    if(TextUtils.isEmpty(phoneNumber)){
                        Toast.makeText(ctx, "you must add a phone number before saving", Toast.LENGTH_SHORT).show();
                    }else {
                        prefManager.setEmergencyContactNumber(phoneNumber);
                        Toast.makeText(ctx, "contact phone number saved successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                mContactOption.setVisibility(View.GONE);
                prefManager.setEmergencyContactNumber(null);
            }
        });

        if(prefManager.isContactNumberSet()){
            mEnableEmergencyCalls.setChecked(true);
            mContactOption.setVisibility(View.VISIBLE);
        }

        boolean alarmType = prefManager.getAlarmType();

        mAlarmTypeChooser.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case R.id.alarm:
                    prefManager.setAlarmType(true);
                    mTTSLayout.setVisibility(View.GONE);
                    break;
                case R.id.speech:
                    mTTSLayout.setVisibility(View.VISIBLE);
                    mSaveMessageBtn.setOnClickListener(v -> {
                        String message = mTTSMessage.getText().toString();
                        if(TextUtils.isEmpty(message)){
                            Toast.makeText(ctx, "you must add a message before saving", Toast.LENGTH_SHORT).show();
                        }else {
                            prefManager.setTTSDefaultMessage(message);
                            prefManager.setAlarmType(false);
                        }
                    });
                    break;
                default:prefManager.setAlarmType(true);
            }
        });

        if(alarmType){
            mAlarmTypeChooser.check(R.id.alarm);
        }else {
            mAlarmTypeChooser.check(R.id.speech);
        }


    }
}