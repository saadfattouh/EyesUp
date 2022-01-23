package com.example.eyesup;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eyesup.helper.SharedPrefManager;

public class WelcomeActivity extends AppCompatActivity {

    Button mLoginBtn;
    Button mSignUpBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mLoginBtn = findViewById(R.id.log_in_btn);
        mSignUpBtn = findViewById(R.id.sign_up_btn);


        if(SharedPrefManager.getInstance(this).isLoggedIn()){
            waitAndGo("you're logged is already!", Toast.LENGTH_SHORT);
        }else{
            Toast.makeText(this, "you are not logged in yet try to register or login if you have an account already", Toast.LENGTH_SHORT).show();
        }

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignUp();
            }
        });

    }

    void waitAndGo(String message, int length){
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pDialog.dismiss();
                Toast.makeText(WelcomeActivity.this, message, length).show();
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        }, 500);
    }

    public void goToLogin()
    {
        Intent login = new Intent(this, LoginActivity.class);
        startActivity(login);
//        finish();
    }

    public void goToSignUp()
    {
        Intent signUp = new Intent(this, SignUpActivity.class);
        startActivity(signUp);
//        finish();
    }
}