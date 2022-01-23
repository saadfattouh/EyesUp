package com.example.eyesup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    EditText mPasswordET;
    EditText mPhoneET;

    Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mPasswordET = findViewById(R.id.password);
        mPhoneET = findViewById(R.id.phone);

        mLogin = findViewById(R.id.log_in_btn);

        mLogin.setOnClickListener(v -> {
            if(verifyInput()){
                mLogin.setEnabled(false);
                userLogin();
            }
        });



    }



    private void goToMainActivity() {

        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
    }


    //offline working
    private boolean verifyInput() {


        //first getting the values
        final String pass = mPasswordET.getText().toString();
        final String phone = mPhoneET.getText().toString();


        //checking if password is empty
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "please enter your phone number!", Toast.LENGTH_SHORT).show();
            mLogin.setEnabled(true);
            return false;
        }


        //checking if password is empty
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "please enter your password!", Toast.LENGTH_SHORT).show();
            mLogin.setEnabled(true);
            return false;
        }


        return true;
    }

    private void userLogin() {

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();


        //first getting the values
        final String pass = mPasswordET.getText().toString();
        final String phone = mPhoneET.getText().toString();

        String url = Constant.USER_LOGIN_URL + "&phone="+phone + "&password="+pass;

        AndroidNetworking.get(url)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        pDialog.dismiss();
                        mLogin.setEnabled(true);

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
                                        "null",
                                        userJson.getString("phone"),
                                        userJson.getString("birthdate")
                                );

                                //storing the user in shared preferences
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(user);
                                finish();
                                goToMainActivity();

                            } else {
                                Toast.makeText(getApplicationContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        mLogin.setEnabled(true);
                    }
                });



    }


}