package com.example.imstargram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SignUpActivity extends AppCompatActivity {


//    LinearLayout loginContatiner;
//    AnimationDrawable animationDrawable;


    EditText etUserEmail, etUserName, etUserPwd, etUserPwdConfirm;
    Button btnSignUp, btnLogin;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


//        loginContatiner = findViewById(R.id.login_container);
//        animationDrawable = (AnimationDrawable) loginContatiner.getBackground();
//        animationDrawable.setEnterFadeDuration(5000);
//        animationDrawable.setEnterFadeDuration(2000);

        etUserEmail = findViewById(R.id.sign_user_email);
        etUserName = findViewById(R.id.sign_user_name);
        etUserPwd = findViewById(R.id.sign_user_password);
        etUserPwdConfirm = findViewById(R.id.sign_user_password_confirm);
        btnSignUp = findViewById(R.id.sign_up_btn);
        btnLogin = findViewById(R.id.sign_login_btn);
        progressDialog = new ProgressDialog(this);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                register();
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });


    }


    private void register(){


        progressDialog.setTitle("Creating your account");
        progressDialog.setMessage("Please wait....");
        progressDialog.show();

        final String email = etUserEmail.getText().toString();
        final String username = etUserName.getText().toString();
        final String password = etUserPwd.getText().toString();
        String password_confirm = etUserPwdConfirm.getText().toString();


        if(!email.contains("@")){
            etUserEmail.setError("This is not a valid email");
            etUserEmail.requestFocus();
            progressDialog.dismiss();
            return;
        }
        if(TextUtils.isEmpty(username)){
            etUserName.setError("Please fill in this field");
            etUserName.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if(TextUtils.isEmpty(password)){
            etUserPwd.setError("Please fill in this field");
            etUserPwd.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if(TextUtils.isEmpty(password_confirm)){
            etUserPwdConfirm.setError("Please fill in this field");
            etUserPwdConfirm.requestFocus();
            progressDialog.dismiss();
            return;
        }

        if(!password.equals(password_confirm)){
            etUserPwd.setError("Password charachters don't match!");
            etUserPwd.requestFocus();
            progressDialog.dismiss();
            return;

        }


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.SignUp_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){
//                                progressDialog.dismiss();




                                JSONObject jsonObjectUser =  jsonObject.getJSONObject("user");

                                User user = new User(jsonObjectUser.getInt("id"),jsonObjectUser.getString("email"),jsonObjectUser.getString("username")
                                        ,jsonObjectUser.getString("image")) ;

                                Log.d("보낸다", "유저" + user);
                                Log.d("보낸다", "이ㅁㄹ" + user.getEmail());
                                Log.d("보낸다", "이름" + user.getUsername());
//                                Log.d("보낸다", "소개" + user.getIntro());



                                //store user data inside sharedprefrences
//                                SharedPrefrenceManger.getInstance(getApplicationContext()).storeUserData(user);


                                //let user in
                                Intent emailVerifyIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                                startActivity(emailVerifyIntent);
                                progressDialog.dismiss();
                                finish();

//                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
//                                finish();



                            }else{

                                Toast.makeText(SignUpActivity.this,jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                            }


                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SignUpActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }


        ){

            @Override
            protected Map<String, String> getParams() {
                Map<String,String> authenticationVariables = new HashMap<>();
                authenticationVariables.put("Content-Type", "application/json charset=utf-8");
                authenticationVariables.put("email",email);
                authenticationVariables.put("username",username);
                authenticationVariables.put("password",password);
                Log.d("회원가입","잘되니" + email);
                Log.d("회원가입","잘되니" + username);
                Log.d("회원가입","잘되니" + password);

                return  authenticationVariables;
            }

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> authenticationVariables = new HashMap<>();
//                authenticationVariables.put("Content-Type", "application/json charset=utf-8");
//                return authenticationVariables;
//            }


        };//end of string Request



        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if(mAnimationDrawable != null && !mAnimationDrawable.isRunning()){
//            mAnimationDrawable.start();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if(mAnimationDrawable != null && mAnimationDrawable.isRunning()){
//            mAnimationDrawable.stop();
//        }
//    }



    @Override
    protected void onStart() {
        super.onStart();

//        boolean isUserLoggedIn = SharedPrefrenceManger.getInstance(getApplicationContext()).isUserLogggedIn();
//
//        if (isUserLoggedIn) {
//            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
//        } else {
//
//        }
    }




}
