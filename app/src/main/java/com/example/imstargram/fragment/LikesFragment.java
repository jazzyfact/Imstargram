package com.example.imstargram.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.imstargram.R;


public class LikesFragment extends Fragment {


    public LikesFragment() {
        // Required empty public constructor
    }


    public static LikesFragment newInstance(String param1, String param2) {
        LikesFragment fragment = new LikesFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_likes, container, false);
    }




}
