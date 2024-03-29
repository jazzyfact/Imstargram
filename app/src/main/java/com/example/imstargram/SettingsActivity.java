package com.example.imstargram;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.imstargram.helper.SharedPrefrenceManger;
import com.example.imstargram.helper.URLS;
import com.example.imstargram.helper.VolleyHandler;
import com.example.imstargram.models.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private static final String TAG = "SettingsActivity";


    TextView tvEditImageBtn, btnDoneEdit;
    EditText etUserName, etDesc, etEmail;
    CircleImageView ivProfileImage;
    ImageView backArrow;

    final int CHANGE_PROFILE_IMAGE = 1;
    ProgressDialog mProgrssDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvEditImageBtn = findViewById(R.id.edit_image);
        btnDoneEdit = findViewById(R.id.done_edit);
        etUserName = findViewById(R.id.username_et);
        etDesc = findViewById(R.id.desc_et);
        etEmail = findViewById(R.id.email_et);
        ivProfileImage = findViewById(R.id.profile_image);
        backArrow = findViewById(R.id.back_arrow);

        mProgrssDialog = new ProgressDialog(this);

        tvEditImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewProfileImage();
            }
        });


        btnDoneEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        String username = getIntent().getStringExtra("username");
        String imageProfile = getIntent().getStringExtra("imageProfile");
        String email = getIntent().getStringExtra("email");
        String intro = getIntent().getStringExtra("intro");

        if (!username.isEmpty()) {
            etUserName.setText(username);
        }

        if (!imageProfile.isEmpty()) {
            Picasso.get().load(imageProfile).error(R.drawable.ic_user).into(ivProfileImage);
        }

        if (!email.isEmpty()) {
            etEmail.setText(email);
        }




        //뒤로가기
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'ProfileActivity'");
                finish();
            }
        });

    }

    //유저프로필 업데이트
    private void updateUserData() {

        final String email = etEmail.getText().toString();
        User user = SharedPrefrenceManger.getInstance(getApplicationContext()).getUserData();
        final int user_id = user.getId();


        mProgrssDialog.setTitle("프로필 업데이트 중..");
        mProgrssDialog.setMessage("잠시만 기다려주세요...");
        mProgrssDialog.show();


        final StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.update_user_data, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.i("userObject", jsonObject.toString());

                    if (!jsonObject.getBoolean("error")) {

                        mProgrssDialog.dismiss();
                        String emailString = jsonObject.getString("email");


                        Log.i("emailString", emailString);

                        if (!emailString.isEmpty()) {
                            etEmail.setText(emailString);
                            //update shared pref email
                            SharedPrefrenceManger.getInstance(getApplicationContext()).updateEmail(emailString);
                        }


                    } else {

                        Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        mProgrssDialog.dismiss();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        mProgrssDialog.dismiss();
                    }
                }


        ) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map params = new HashMap<>();
                params.put("email", email);
                params.put("user_id", String.valueOf(user_id));

                return params;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);


    }


    private void getNewProfileImage() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, CHANGE_PROFILE_IMAGE);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHANGE_PROFILE_IMAGE) {

            if (resultCode == RESULT_OK) {

                Uri uri = data.getData();
                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    if (bitmap != null) {
                        sendImageToDataBase(bitmap);
                    } else {
                        Toast.makeText(SettingsActivity.this, "오류", Toast.LENGTH_LONG).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            } else if (resultCode == RESULT_CANCELED) {


            } else {


            }

        }

    }


    private void sendImageToDataBase(Bitmap bitmap) {

        final String imageString = convertImageToString(bitmap);
        User user = SharedPrefrenceManger.getInstance(getApplicationContext()).getUserData();
        final int user_id = user.getId();

        final String image_name = dateOfImage();

        if (imageString != null) {


            mProgrssDialog.setTitle("프로필 사진 업데이트 중");
            mProgrssDialog.setMessage("잠시만 기다려 주세요....");
            mProgrssDialog.show();


            StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.update_profile_image, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Log.i("imageObject", jsonObject.toString());

                        if (!jsonObject.getBoolean("error")) {
                            mProgrssDialog.dismiss();
                            String imageString = jsonObject.getString("image");


                            Log.i("imageRespose", imageString);

                            if (!imageString.isEmpty()) {
                                Picasso.get().load(imageString).error(R.drawable.ic_user).into(ivProfileImage);
                                //update profile image
                                SharedPrefrenceManger.getInstance(getApplicationContext()).updateProfileImage(imageString);

                            }


                        } else {

                            Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            mProgrssDialog.dismiss();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                            mProgrssDialog.dismiss();
                        }
                    }


            ) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map imageMap = new HashMap<>();
                    imageMap.put("image_encoded", imageString);
                    imageMap.put("image_name", image_name);
                    imageMap.put("user_id", String.valueOf(user_id));

                    return imageMap;
                }
            };//end of string Request

            VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);


        }


    }

    private String dateOfImage() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.toString();

    }


    private String convertImageToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageByteArray = baos.toByteArray();
        String result = Base64.encodeToString(imageByteArray, Base64.DEFAULT);
        return result;
    }
}

