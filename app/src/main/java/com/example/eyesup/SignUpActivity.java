package com.example.eyesup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.eyesup.api.Constant;
import com.example.eyesup.helper.SharedPrefManager;
import com.example.eyesup.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {


    EditText mUserNameET;
    EditText mPasswordET;
    EditText mPhoneET;
    EditText mAddressET;
    ImageButton mBirthDateBtn;
    Button mSignUpBtn;

    String selectedDate = "";

    String verificationMessage = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mUserNameET = findViewById(R.id.user_name);
        mPasswordET = findViewById(R.id.password);
        mPhoneET = findViewById(R.id.phone);
        mAddressET = findViewById(R.id.address);
        mBirthDateBtn = findViewById(R.id.birth_date);

        mSignUpBtn = findViewById(R.id.sign_up_btn);

        mBirthDateBtn.setOnClickListener(v -> openCalender());


        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyInput()){
                    mSignUpBtn.setEnabled(false);
                    registerUser();
                }
            }
        });

    }

    private void openCalender() {
        final Calendar newCalendar = Calendar.getInstance();
        final DatePickerDialog StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                selectedDate = String.format(Locale.US, "%04d-%02d-%02d", newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH));
                Toast.makeText(SignUpActivity.this, selectedDate, Toast.LENGTH_SHORT).show();
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        StartTime.show();
    }

    //attention! the extra parameter is an integer
    private void goToVerifyAccount() {
        Intent verify = new Intent(this, VerificationActivity.class);
        startActivity(verify);
    }

    //api call
    private void registerUser() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        //first getting the values
        final String userName = mUserNameET.getText().toString();
        final String pass = mPasswordET.getText().toString();
        final String phone = mPhoneET.getText().toString();
        final String address = mAddressET.getText().toString();
        final String birthDate = selectedDate;

        String url = Constant.USER_REGISTER_URL + "&username="+userName + "&password="+pass + "&phone="+phone + "&birthdate="+birthDate + "&address="+address;

        AndroidNetworking.get(url)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (!obj.getBoolean("error")) {

                                //getting the user from the response
                                JSONObject userJson = obj.getJSONObject("data");
                                User user;
                                user = new User(
                                        userJson.getInt("id"),
                                        userJson.getString("username"),
                                        userJson.getString("address"),
                                        userJson.getString("phone"),
                                        userJson.getString("birthdate")
                                );

                                //storing the user in shared preferences
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                                finish();
                                goToVerifyAccount();

                                mSignUpBtn.setEnabled(true);
                            } else {
                                Toast.makeText(getApplicationContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                mSignUpBtn.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mSignUpBtn.setEnabled(true);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        mSignUpBtn.setEnabled(true);
                    }
                });


    }

    //offline working
    private boolean verifyInput() {


        //first getting the values
        final String userName = mUserNameET.getText().toString();
        final String pass = mPasswordET.getText().toString();
        final String phone = mPhoneET.getText().toString();
        final String address = mAddressET.getText().toString();
        final String birthDate = selectedDate;

        //checking if username is empty
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "please enter userName!", Toast.LENGTH_SHORT).show();
            mSignUpBtn.setEnabled(true);
            return false;
        }

        //checking if password is empty
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "please enter your password!", Toast.LENGTH_SHORT).show();
            mSignUpBtn.setEnabled(true);
            return false;
        }


        //checking if password is empty
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "please enter your phone number!", Toast.LENGTH_SHORT).show();
            mSignUpBtn.setEnabled(true);
            return false;
        }

        //checking if password is empty
        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "please enter your address!", Toast.LENGTH_SHORT).show();
            mSignUpBtn.setEnabled(true);
            return false;
        }


        if (TextUtils.isEmpty(birthDate)) {
            Toast.makeText(this, "please enter your birthDate", Toast.LENGTH_SHORT).show();
            mSignUpBtn.setEnabled(true);
            return false;
        }


        return true;
    }




}