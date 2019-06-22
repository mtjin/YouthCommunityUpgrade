package com.mtjinse.myapplication.activity.fragments;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.adapters.BoardAdapter;
import com.mtjinse.myapplication.activity.models.Board;

import java.util.ArrayList;

public class Tab2Fragment extends Fragment {
    ViewGroup rootView;
    //xml
    private RecyclerView mBoardRecyclerView;
    private ArrayList<Board> mBoardArrayList;
    private BoardAdapter mBoardAdapter;
    //value
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public Tab2Fragment() {
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
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tab2, container, false);
        mBoardRecyclerView = rootView.findViewById(R.id.tab2_rev_board);

        mBoardArrayList = new ArrayList<>();
        final String[] boards = getResources().getStringArray(R.array.boards); //게시판 제목들
        for(String boardName : boards){
            mBoardArrayList.add(new Board(boardName));
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mBoardRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mBoardRecyclerView.setLayoutManager(layoutManager);
        mBoardAdapter = new BoardAdapter(mBoardArrayList, getActivity());

        mBoardRecyclerView.setAdapter(mBoardAdapter);

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}