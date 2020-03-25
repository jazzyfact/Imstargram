package com.example.imstargram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.imstargram.fragment.CameraFragment;
import com.example.imstargram.fragment.HomeFragment;
import com.example.imstargram.fragment.LikesFragment;
import com.example.imstargram.fragment.ProfileFragment;
import com.example.imstargram.fragment.SearchFragment;
import com.example.imstargram.helper.SharedPrefrenceManger;
import com.example.imstargram.helper.URLS;
import com.example.imstargram.helper.VolleyHandler;
import com.example.imstargram.models.User;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    User user;
//    String ivProfileImage, strEmail, strUserName;
    String mImageProfile,mEmail,mUsername ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        drawerLayout = findViewById(R.id.main_drawer_layout);
        navigationView = findViewById(R.id.main_nav_view);


        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        setSupportActionBar((Toolbar) findViewById(R.id.my_toolbar));
        getSupportActionBar().setTitle("Imstagram");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home_menu);


        user = SharedPrefrenceManger.getInstance(this).getUserData();




        //프래그먼트 디폴트 값
        changeFragmentDisplay(R.id.home);



        //네비게이션뷰 리스너
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                changeFragmentDisplay(menuItem.getItemId());
                return true;

            }
        });


    }


    //네비게이션 프로필
    @SuppressLint("WrongConstant")
    private boolean changeFragmentDisplay(int menuItem) {

        Fragment fragment = null;

        if (menuItem == R.id.home) {
            fragment = new HomeFragment();

            Toast.makeText(MainActivity.this, "홈", Toast.LENGTH_LONG).show();
//                    drawerLayout.closeDrawer();

        } else if (menuItem == R.id.search) {

            startActivity(new Intent(MainActivity.this,SearchActivity.class));
            Toast.makeText(MainActivity.this, "검색", Toast.LENGTH_LONG).show();

        } else if (menuItem == R.id.camera) {

            fragment = new CameraFragment();
            Toast.makeText(MainActivity.this, "카메라", Toast.LENGTH_LONG).show();

        } else if (menuItem == R.id.likes) {

            fragment = new LikesFragment();
            Toast.makeText(MainActivity.this, "좋아요", Toast.LENGTH_LONG).show();

        } else if (menuItem == R.id.profile) {

            fragment = new ProfileFragment();
            Toast.makeText(MainActivity.this, "프로필", Toast.LENGTH_LONG).show();

        } else if (menuItem == R.id.logout) {

            logUserOutIFTheyWant();
            Toast.makeText(MainActivity.this, "log out", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
        }


        //네비게이션 뷰 숨기기
        drawerLayout.closeDrawer(Gravity.START);


        //프래그먼트
        if(fragment != null){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
           fragmentTransaction.replace(R.id.main_fragment_content, fragment);
           fragmentTransaction.commit();

        }

        return false;
    }



    //로그아웃
    private void logUserOutIFTheyWant(){


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("로그아웃");
        builder.setMessage("로그아웃 하시겠습니까?");

        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPrefrenceManger sharedPrefrenceManger = SharedPrefrenceManger.getInstance(getApplicationContext());
                sharedPrefrenceManger.logUserOut();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                dialog.dismiss();


            }
        });


        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }else if(item.getItemId() == R.id.setting){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            settingsIntent.putExtra("imageProfile",mImageProfile);
            settingsIntent.putExtra("email",mEmail);
            settingsIntent.putExtra("username",mUsername);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }







    @Override
    protected void onStart() {
        super.onStart();

        boolean isUserLoggedIn = SharedPrefrenceManger.getInstance(getApplicationContext()).isUserLogggedIn();

        //check if user is logged in
        if (!isUserLoggedIn) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {

            getUserData();



        }



    }




    @Override
    protected void onPause() {
        super.onPause();
        Log.i("onpause","pause");

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onresume","resume");

   /*     boolean isUserLoggedIn = SharedPrefrenceManger.getInstance(getApplicationContext()).isUserLogggedIn();

        if (!isUserLoggedIn) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {
            getUserData();

        }
        */


    }



    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("onrestart","restart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("onstop","stop");
    }


    private void getUserData(){


        View navHeader = navigationView.getHeaderView(0);
        final ImageView user_iv = navHeader.findViewById(R.id.user_iv);
        final TextView user_name = navHeader.findViewById(R.id.user_name);
        final TextView user_email = navHeader.findViewById(R.id.user_email);

        int user_id = user.getId();
        String username = user.getUsername();
        user_name.setText(username);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.get_user_data+user_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("userObjectProfile",jsonObject.toString());

                            if(!jsonObject.getBoolean("error")){


                                JSONObject jsonObjectUser =  jsonObject.getJSONObject("user");

                                // String ivProfileImage, strEmail, strUserName;

                                mUsername  = jsonObjectUser.getString("username");
                                mEmail  = jsonObjectUser.getString("email");
                                mImageProfile= jsonObjectUser.getString("image");


                                user_email.setText(mEmail);
                                Log.d("MainActivity", "이메일" +  mEmail + " ");

                                if(!mImageProfile.isEmpty()){
                                    Picasso.get().load(mImageProfile).error(R.drawable.ic_user).into(user_iv);
                                    Log.d("MainActivity", "이미지" +  user_iv + " ");
                                }

                            }else{

                                Toast.makeText(MainActivity.this,jsonObject.getString("message"),Toast.LENGTH_LONG).show();

                            }


                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();

                    }
                }


        );

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);

    }







}
