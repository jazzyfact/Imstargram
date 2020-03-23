package com.example.imstargram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.imstargram.helper.URLS;
import com.example.imstargram.helper.SharedPrefrenceManger;
import com.example.imstargram.helper.VolleyHandler;
import com.example.imstargram.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

//    LinearLayout loginContatiner;
//    AnimationDrawable animationDrawable;


    EditText etUserEmail, etPassWord;
    TextView tvForGetPwd;
    ProgressDialog progressDialog;
    Button btnLogin, btnSignUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


//        loginContatiner = findViewById(R.id.login_container);
//        animationDrawable = (AnimationDrawable) loginContatiner.getBackground();
//        animationDrawable.setEnterFadeDuration(5000);
//        animationDrawable.setEnterFadeDuration(2000);


        etUserEmail = findViewById(R.id.login_user_email);
        etPassWord = findViewById(R.id.login_user_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_sign_up);
        tvForGetPwd = findViewById(R.id.forgot_pass);
        progressDialog = new ProgressDialog(this);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logIn();
            }
        });


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);

            }
        });


        tvForGetPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }


    private void logIn() {

        progressDialog.setTitle("Log In");
        progressDialog.setMessage("Please wait....");
        progressDialog.show();

        final String username = etUserEmail.getText().toString();
        final String password = etPassWord.getText().toString();

        if (TextUtils.isEmpty(username)) {
            etUserEmail.setError("Please fill in this field");
            etUserEmail.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassWord.setError("Please fill in this field");
            etPassWord.requestFocus();
            progressDialog.dismiss();
            return;
        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.Login_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {

                                progressDialog.dismiss();

                                JSONObject jsonObjectUser = jsonObject.getJSONObject("user");

                                User user = new User(jsonObjectUser.getInt("id"), jsonObjectUser.getString("email")
                                        , jsonObjectUser.getString("username"), jsonObjectUser.getString("image"));


                                //store user data inside sharedprefrences
                                SharedPrefrenceManger.getInstance(getApplicationContext()).storeUserData(user);


                                //let user in
                                finish();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));

                            } else {

                                Toast.makeText(LoginActivity.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }


        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> authenticationVariables = new HashMap<>();
                authenticationVariables.put("username", username);
                authenticationVariables.put("password", password);
                return authenticationVariables;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);


    }


    @Override
    protected void onStart() {
        super.onStart();

        boolean isUserLoggedIn = SharedPrefrenceManger.getInstance(getApplicationContext()).isUserLogggedIn();

        if (isUserLoggedIn) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else {

        }
    }


}
