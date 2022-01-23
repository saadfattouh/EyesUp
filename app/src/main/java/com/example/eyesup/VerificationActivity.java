package com.example.eyesup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.eyesup.api.Constant;
import com.example.eyesup.helper.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

public class VerificationActivity extends AppCompatActivity {


    EditText mVerificationCodeET;
    Button mVerifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mVerificationCodeET = findViewById(R.id.verification_code_edit_text);
        mVerifyBtn = findViewById(R.id.verify_btn);

        mVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyInput()){
                    mVerifyBtn.setEnabled(false);
                    verifyAccount();
                }
            }
        });

    }

    private boolean verifyInput() {

        String code = mVerificationCodeET.getText().toString();

        if(TextUtils.isEmpty(code)){
            Toast.makeText(this, "please enter your 6-digits verification code!", Toast.LENGTH_SHORT).show();
            mVerifyBtn.setEnabled(true);
            return false;
        }

        return true;
    }


    private void verifyAccount() {
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        //first getting the values
        final String code = mVerificationCodeET.getText().toString();

        String url = Constant.USER_VERIFY_URL + "&phone="+ SharedPrefManager.getInstance(this).getUserPhone() + "&code="+code;

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

                                finish();
                                Toast.makeText(VerificationActivity.this, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                goToMainActivity();

                                mVerifyBtn.setEnabled(true);
                            } else {
                                Toast.makeText(getApplicationContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                mVerifyBtn.setEnabled(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mVerifyBtn.setEnabled(true);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        mVerifyBtn.setEnabled(true);
                    }
                });


    }

    private void goToMainActivity() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
    }
}