package com.example.imstargram.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.imstargram.R;
import com.example.imstargram.adapter.StoryListAdapter;
import com.example.imstargram.helper.SharedPrefrenceManger;
import com.example.imstargram.helper.URLS;
import com.example.imstargram.helper.VolleyHandler;
import com.example.imstargram.models.Story;
import com.example.imstargram.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HomeFragment extends Fragment {

    ListView feed_lv;
    ArrayList<Story> arrayListStories;
    StoryListAdapter storyListAdapter;
    ProgressDialog mProgrssDialog;
    JSONArray jsonArrayIds;
    SwipeRefreshLayout swipeRefreshLayout; //페이징, 당겨서
    String allUserIds;
    int offset;
    int limit = 2;
    int location;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  view = inflater.inflate(R.layout.fragment_home, container, false);
        feed_lv = view.findViewById(R.id.feed_lv);


        //페이징
        swipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        offset = 0;
        location = 0;

        arrayListStories = new ArrayList<Story>();

        mProgrssDialog = new ProgressDialog(getContext());
        mProgrssDialog.setTitle("새로운 게시물");
        mProgrssDialog.setMessage("게시물 업데이트 중입니다...");

        storyListAdapter = new StoryListAdapter(getContext(),R.layout.feed_single_item,arrayListStories);
        feed_lv.setAdapter(storyListAdapter);


        getFollowingIds();

        return view;





    }


    //swiperRefreshlaout  페이징
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //리프레쉬 코드
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                offset = offset + limit;
                location = 0;
                getLatestNewsFeed(allUserIds);
                swipeRefreshLayout.setRefreshing(false); // 당겨서 새로고침,  아이콘 숨기기
            }
        });

    }

    private void getFollowingIds(){

        mProgrssDialog.show();


        User user = SharedPrefrenceManger.getInstance(getContext()).getUserData();
        final int user_id = user.getId();
        Log.i("userId",String.valueOf(user_id));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.get_following_ids+user_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            mProgrssDialog.dismiss();

                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                JSONArray jsonArrayIds =  jsonObject.getJSONArray("ids");

                                Log.i("arrayids",jsonArrayIds.toString());
                                //arrayid : [6,8,10,12]

                                String ids = jsonArrayIds.toString();
                                ids = ids.replace("[","");
                                ids = ids.replace("]","");
                                //ids = ids + "," + user_id;
                                allUserIds = ids;

                                Log.i("arrayidsNew",ids);
                                getLatestNewsFeed(ids);

                            }else{

                                Toast.makeText(getContext(),jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                                mProgrssDialog.dismiss();
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
                        mProgrssDialog.dismiss();
                    }
                }


        );


        VolleyHandler.getInstance(getContext().getApplicationContext()).addRequetToQueue(stringRequest);


    }






    //최근 게시물 가져오는 메소드
    //유저아이디, 좋아요 갯수, 이미지 경로, 제목, 시간, 프로필 사진, 닉네임
    private void getLatestNewsFeed(String ids){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.lasted_news_feed+ids+"&limit="+limit+"&offset="+offset,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                mProgrssDialog.dismiss();

                                JSONArray jsonObjectStories =  jsonObject.getJSONArray("stories");

                                Log.i("arrayStories",jsonObjectStories.toString());

                                for(int i = 0 ; i<jsonObjectStories.length(); i++){
                                    JSONObject jsonObjectSingleStory = jsonObjectStories.getJSONObject(i);
                                    Log.i("jsonsinglestory",jsonObjectSingleStory.toString());
                                    Story story = new Story(jsonObjectSingleStory.getInt("id"),jsonObjectSingleStory.getInt("user_id")
                                            ,jsonObjectSingleStory.getInt("num_of_likes"),jsonObjectSingleStory.getString("image_url")
                                            ,jsonObjectSingleStory.getString("title"),jsonObjectSingleStory.getString("time")
                                            , jsonObjectSingleStory.getString("profile_image"),jsonObjectSingleStory.getString("username"));


                                    arrayListStories.add(location++,story);
                                    //arrayListStories.add(story);
                                    //storyListAdapter.add(story);
                                }

                                //업데이트
                                storyListAdapter.notifyDataSetChanged();

                            }else{

                                Toast.makeText(getContext(),jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                                mProgrssDialog.dismiss();
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
                        mProgrssDialog.dismiss();
                    }
                }


        );

        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
        // Volley.newRequestQueue(getContext().getApplicationContext()).add(stringRequest);

    }








}
