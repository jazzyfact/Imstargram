package com.example.imstargram.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.imstargram.R;
import com.example.imstargram.SettingsActivity;
import com.example.imstargram.adapter.ImageArrayAdapter;
import com.example.imstargram.helper.URLS;
import com.example.imstargram.helper.SharedPrefrenceManger;
import com.example.imstargram.helper.VolleyHandler;
import com.example.imstargram.models.Image;
import com.example.imstargram.models.User;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


//프로필 프래그먼트
//유저의 프로필, 포스트 갯수, 팔로잉, 팔로워, 닉넴, 하고싶은 말, 내가 올린 포스트 사진을 보여줌

public class ProfileFragment extends Fragment {

    TextView tvProfileBtn, tvPostsNum, tvFollowingNum, tvFollowerNum, tvDisplayName,description;
    CircleImageView ivUserProfileImage;
    GridView imagesGridLayout;
    ArrayList<Image> arrayListImages;
    ImageArrayAdapter imageArrayAdapter;
    User user;
    int user_id,posts,following,followers;
    String email,image;


//    public ProfieFragment() {
//        // Required empty public constructor
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvProfileBtn = view.findViewById(R.id.follow_this_profile);
        tvPostsNum =  (TextView) view.findViewById(R.id.posts_num_tv);
        tvFollowingNum =  (TextView) view.findViewById(R.id.following_num_tv);
        tvFollowerNum =  (TextView) view.findViewById(R.id.followers_num_tv);
        tvDisplayName =  (TextView) view.findViewById(R.id.display_name_tv);
        description =  (TextView) view.findViewById(R.id.description);
        ivUserProfileImage = (CircleImageView) view.findViewById(R.id.profile_image);
        imagesGridLayout = (GridView) view.findViewById(R.id.images_grid_layout);

        tvProfileBtn.setText("Edit Profile");

        //로그인한 유저 닉넴
        user = SharedPrefrenceManger.getInstance(getContext()).getUserData();
        user_id = user.getId();

        //이미지를 어레이리스트에 담음
        arrayListImages = new ArrayList<>();


        //유저 정보 가져 오는 메소드
        getUserData();

        //서버에서 내가 포스팅 한 이미지 모두 가져오기
        getAllImages();

        imageArrayAdapter = new ImageArrayAdapter(getContext(),0,arrayListImages);
        imagesGridLayout.setAdapter(imageArrayAdapter);



        return view;
    }



    //설정 화면으로 이동
    // 유저 아이디, 닉네임, 메일, 이미지 ,팔로우, 팔로잉, 포스트 숫자를 인텐트로 넘겨줌
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });
    }

    private void goToSettings(){
        Intent settingsIntenet = new Intent(getContext(), SettingsActivity.class);

        settingsIntenet.putExtra("user_id",user.getId());
        settingsIntenet.putExtra("username",user.getUsername());
        settingsIntenet.putExtra("email",email);
        settingsIntenet.putExtra("imageProfile",image);
        settingsIntenet.putExtra("following",following);
        settingsIntenet.putExtra("followers",followers);
        settingsIntenet.putExtra("posts",posts);

        getContext().startActivity(settingsIntenet);

    }


    //프로필 프래그먼트 화면에서
    //db에 저장되어 있는 이미지를 모두 가져옴
    //반복문을 통해서 이미지 경로, 이미지 이름, 사용자 유저 번호(고유번호)를 가져온다

    private void getAllImages(){


        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.get_all_images+user_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){


                                JSONArray jsonObjectImages =  jsonObject.getJSONArray("images");

                                Log.i("arrayImages",jsonObjectImages.toString());

                                for(int i = 0 ; i<jsonObjectImages.length(); i++){
                                    JSONObject jsonObjectSingleImage = jsonObjectImages.getJSONObject(i);
                                    Log.i("jsonsingleImage",jsonObjectSingleImage.toString());

                                    Image image = new Image(jsonObjectSingleImage.getInt("id")
                                            ,jsonObjectSingleImage.getString("image_url"), jsonObjectSingleImage.getString("image_name")
                                            ,jsonObjectSingleImage.getInt("user_id"));;

                                    arrayListImages.add(image);


                                }

                                //새로 추가 될 때 마다 아이템 전환
                                imageArrayAdapter.notifyDataSetChanged();

                            }else{

                                Toast.makeText(getContext(),jsonObject.getString("message"),Toast.LENGTH_LONG).show();

                            }


                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_LONG).show();

                    }
                }


        );

        VolleyHandler.getInstance(getContext().getApplicationContext()).addRequetToQueue(stringRequest);




    }


    //유저의 정보를 가져오는 메소드

    private void getUserData(){

        //쉐어드에 저장되어있는 닉네임을 가져옴
        String username = user.getUsername();

        //표시
        tvDisplayName.setText(username);

        //유저 정보들을 요청해서 가져옴
        //포스팅 갯수, 팔로잉, 팔로워, 메일주소, 사진을 요청한 후 프래그먼트 화면에 보여줌
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.get_user_data+user_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.i("userObjectProfile",jsonObject.toString());

                            if(!jsonObject.getBoolean("error")){


                                JSONObject jsonObjectUser =  jsonObject.getJSONObject("user");

                                posts = jsonObjectUser.getInt("posts");
                                following = jsonObjectUser.getInt("following");
                                followers = jsonObjectUser.getInt("followers");
                                email  = jsonObjectUser.getString("email");
                                image = jsonObjectUser.getString("image");

                                description.setText(email);
                                tvPostsNum.setText(String.valueOf(jsonObjectUser.getInt("posts")));
                                tvFollowingNum.setText(String.valueOf(jsonObjectUser.getInt("following")));
                                tvFollowerNum.setText(String.valueOf(jsonObjectUser.getInt("followers")));

                                //만약에 프로필 사진이 없다면 기본 디폴트 값인 사진을 가져옴
                                if(!image.isEmpty()){
                                    Picasso.get().load(image).error(R.drawable.ic_user).into(ivUserProfileImage);}

                            }else{

                                Toast.makeText(getContext(),jsonObject.getString("message"),Toast.LENGTH_LONG).show();

                            }


                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),error.getMessage(),Toast.LENGTH_LONG).show();

                    }
                }


        );

        VolleyHandler.getInstance(getContext().getApplicationContext()).addRequetToQueue(stringRequest);



    }

}
