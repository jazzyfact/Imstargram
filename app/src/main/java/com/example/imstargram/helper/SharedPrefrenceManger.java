package com.example.imstargram.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.imstargram.LoginActivity;
import com.example.imstargram.models.User;

public class SharedPrefrenceManger {

    private  static final String FILENAME = "LOGIN";
    private  static final String USERNAME = "username";
    private  static final String EMAIL = "useremail";
    private static final String IMAGE = "image";
    private  static final String ID = "id";

    private static SharedPrefrenceManger sharedPrefrenceManger;
    private Context context;

    private SharedPrefrenceManger(Context context){
        this.context = context;
    }

    public static synchronized SharedPrefrenceManger getInstance(Context context) {

        if (sharedPrefrenceManger == null) {
            sharedPrefrenceManger = new SharedPrefrenceManger(context);

        }
        return sharedPrefrenceManger;
    }



    public void updateProfileImage(String imageUrl){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IMAGE,imageUrl);
        editor.apply();

    }

    public void updateEmail(String email){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL,email);
        editor.apply();

    }


    public void storeUserData(User user){

        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME, user.getUsername());
        editor.putString(EMAIL, user.getEmail());
        editor.putString(IMAGE, user.getImage());
        editor.putInt(ID, user.getId());
        editor.apply();

    }



    //자동로그인
    public boolean isUserLogggedIn(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);

        if(sharedPreferences.getString(EMAIL,null) != null){
            return true;
        }

        return false;
    }


    //로그아웃
    public void logUserOut(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
//        context.startActivity(new Intent(context, LoginActivity.class));
    }


    public User getUserData(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        User user = new User(sharedPreferences.getInt(ID,-1),sharedPreferences.getString(EMAIL,null)
                ,sharedPreferences.getString(USERNAME,null),sharedPreferences.getString(IMAGE,null));
        return user;
    }








}
