package com.example.imstargram.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.example.imstargram.helper.URLS;
import com.example.imstargram.R;
import com.example.imstargram.helper.SharedPrefrenceManger;
import com.example.imstargram.helper.VolleyHandler;
import com.example.imstargram.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class CameraFragment extends Fragment {

    Button btnUpload, btnCapture;
    ImageView ivImageView;
    Uri mImageUri;
    final int CAPTURE_IMAGE = 1, GALLARY_PICK = 2;
    Bitmap bitmap;
    String strStoryTitle, imageToString, strProfileImage;
    boolean OkToUpload;
    String utf8Strin;


    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        btnUpload = (Button) view.findViewById(R.id.upload_btn);
       // btnCapture = (Button) view.findViewById(R.id.capute_btn);
        ivImageView = (ImageView) view.findViewById(R.id.captured_iv);

        OkToUpload = false;


        return view;


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        getProfileImage();

        ivImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String[] options = {"갤러리", "카메라"};
                AlertDialog.Builder build = new AlertDialog.Builder(v.getContext());
                build.setTitle("사진을 선택하세요");
                build.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        switch (which) {
                            //choose from gallery
                            case 0:
                                Intent galleryIntent = new Intent();
                                galleryIntent.setType("image/*");
                                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLARY_PICK);

                                break;

                            //take a photo using camera
                            case 1:

                                capturePhoto();

                                break;


                        }


                    }
                });

                build.show();


            }
        });


        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                storyAndImageTitle();

            }
        });


    }


    private void capturePhoto() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String imageName = "image.jpg";
        // mImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),imageName));
        //File file = new File(Environment.getExternalStorageDirectory(),imageName);
        //mImageUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", file);
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,mImageUri);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE) {

            if (resultCode == RESULT_OK) {


                OkToUpload = true;
                bitmap = (Bitmap) data.getExtras().get("data");
                Toast.makeText(getContext(), "업로드 버튼을 눌러주세요", Toast.LENGTH_LONG).show();
                if (bitmap != null) {
                    ivImageView.setImageBitmap(bitmap);
                }


            }
        }


        if (requestCode == GALLARY_PICK) {

            if (resultCode == RESULT_OK) {


                OkToUpload = true;
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    Toast.makeText(getContext(), "업로드 버튼을 눌러주세요", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ivImageView.setImageBitmap(bitmap);

            }


        }
    }

    private void storyAndImageTitle() {

        final EditText editText = new EditText(getContext());
        editText.setTextColor(Color.BLACK);
        editText.setHint("게시물 내용을 작성해주세요");
        editText.setHintTextColor(Color.GRAY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("게시물 제목");
        builder.setCancelable(false);
        builder.setView(editText);

        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                if (OkToUpload) {
                    strStoryTitle = editText.getText().toString();
                    imageToString = convertImageToString();
                    uploadStory();
                } else {
                    Toast.makeText(getContext(), "사진을 먼저 선택해주세요", Toast.LENGTH_LONG).show();
                }


            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();


    }

    private String convertImageToString() {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageByteArray = baos.toByteArray();
        String result = Base64.encodeToString(imageByteArray, Base64.DEFAULT);

        return result;


    }


    private void getProfileImage() {


        User user = SharedPrefrenceManger.getInstance(getContext()).getUserData();
        int user_id = user.getId();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.get_user_data + user_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {

                                JSONObject jsonObjectUser = jsonObject.getJSONObject("user");

                                strProfileImage = jsonObjectUser.getString("image");


                            } else {

                                Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }


        ) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {

                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    // log error
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    // log error
                    return Response.error(new ParseError(e));
                }

            }
        };


        VolleyHandler.getInstance(getContext().getApplicationContext()).addRequetToQueue(stringRequest);
    }

    private void uploadStory() {


        if (!OkToUpload) {
            Toast.makeText(getContext(), "사진이 없습니다", Toast.LENGTH_LONG).show();

            return;
        }


        final String dateOfImage = dateOfImage();
        final String currentTime = currentReadableTime();
        User user = SharedPrefrenceManger.getInstance(getContext()).getUserData();
        final String username = user.getUsername();
        final int user_id = user.getId();
        final String profile_image = strProfileImage;


        final ProgressDialog mProgrssDialog = new ProgressDialog(getContext());
        mProgrssDialog.setTitle("게시물 업로드 중..");
        mProgrssDialog.setMessage("잠시만 기다려주세요....");
        mProgrssDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.upload_story_image,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                mProgrssDialog.dismiss();


                                Toast.makeText(getContext(), "게시물 업로드 성공", Toast.LENGTH_LONG).show();

                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.main_fragment_content, new HomeFragment());
                                ft.commit();


                            } else {

                                Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        mProgrssDialog.dismiss();
                    }
                }) {

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {

                try {
                    String utf8String = new String(response.data, "UTF-8");
                    Log.d("한글", "한글제목1" + utf8String);
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    // log error
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    // log error
                    return Response.error(new ParseError(e));
                }

            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> imageMap = new HashMap<>();
                imageMap.put("image_name", dateOfImage);
                imageMap.put("image_encoded", imageToString);
                imageMap.put("title", strStoryTitle);
                imageMap.put("time", currentTime);
                imageMap.put("username", username);
                imageMap.put("user_id", String.valueOf(user_id));
                imageMap.put("profile_image", profile_image);
                Log.d("한글", "한글제목2" + strStoryTitle);
                return imageMap;

            }
        };//end of string Request

        VolleyHandler.getInstance(getContext().getApplicationContext()).addRequetToQueue(stringRequest);

        OkToUpload = false;


    }


    private String dateOfImage() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Log.d("사진", "사진" + timestamp.toString());
        return timestamp.toString();
    }


    private String currentReadableTime() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Date date = new Date(timestamp.getTime());
        Log.d("사진", "사진" + date.toString());

        return date.toString();

//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        Log.d("사진", "사진" + timestamp.toString());
//        return timestamp.toString();

//        Date currentTime = Calendar.getInstance().getTime();
//        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일", Locale.getDefault()).format(currentTime);
//
//        Log.d("webnautes", date_text);
//        return date_text.toString();


    }


}