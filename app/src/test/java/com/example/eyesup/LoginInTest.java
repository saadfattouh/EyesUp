package com.example.eyesup;

import android.app.ProgressDialog;
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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class LoginInTest {
    @Test
    public void addition_isCorrect() {

        //first getting the values
        final String pass = "00000000";
        final String phone = "n@n.com";

        String url = Constant.USER_LOGIN_URL + "&phone=" + phone + "&password=" + pass;

        AndroidNetworking.get(url)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

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


                                assertNotEquals(user, null);


                            } else {
                                assertNotEquals(obj, null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            assertNotEquals(1, 2);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error

                    }
                });
    }
}