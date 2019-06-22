package com.mtjinse.myapplication.activity.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.adapters.FriendListAdapter;
import com.mtjinse.myapplication.activity.adapters.ReceiveFriendListAdapter;
import com.mtjinse.myapplication.activity.models.Profile;

import java.util.ArrayList;


public class Tab0Fragment extends Fragment {
    final static String TAG = "Tab0TAG";
    //xml
    ViewGroup rootView;
    private RecyclerView mFriendsRecyclerView;
    private RecyclerView mReceiveFriendRecyclerView;
    //value
    private ArrayList<Profile> mFriendList;
    private ArrayList<Profile> mReceiveFriendList;
    private FriendListAdapter mFriendListAdapter;
    private ReceiveFriendListAdapter mReceiveFriendListAdapter;
    private String mProfileNickName; // 내 닉네임
    private String mProfileUid =  FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public Tab0Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tab0, container, false);
        loadProfileSharedPreferences(); //정보불러오기기
        mFriendsRecyclerView = rootView.findViewById(R.id.tab0_rev_friends);
        mReceiveFriendRecyclerView = rootView.findViewById(R.id.tab0_rev_receivefriend);

        //친구목록 리사이클러뷰세팅
        //아이템리스트
        mFriendList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        mFriendsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        mFriendsRecyclerView.setLayoutManager(layoutManager); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
        //어댑터를 연결시켜준다.
        mFriendListAdapter = new FriendListAdapter(mFriendList, getActivity());
        mFriendsRecyclerView.setAdapter(mFriendListAdapter);

        //친구요청 리사이클러뷰 세팅
        //아이템리스트
        mReceiveFriendList = new ArrayList<>();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        mReceiveFriendRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        mReceiveFriendRecyclerView.setLayoutManager(layoutManager2); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
        //어댑터를 연결시켜준다.
        mReceiveFriendListAdapter = new ReceiveFriendListAdapter(mReceiveFriendList, getActivity());
        mReceiveFriendRecyclerView.setAdapter(mReceiveFriendListAdapter);

        //친구 목록 불러오기
        loadFriendListFromDB();

        //친구요청 목록 불러오기
        loadFriendRequestListFromDB();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mReceiveFriendListAdapter.notifyDataSetChanged();
        mFriendListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // 쉐어드값을 불러오는 메소드
    private void loadProfileSharedPreferences() {
        mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();   //사용자 고유 토큰 받아옴
        SharedPreferences pref = getActivity().getSharedPreferences(mProfileUid + "profile", getActivity().MODE_PRIVATE);
        mProfileNickName = pref.getString("proNickName", "");
    }

    //친구목록 불러오기
    private void loadFriendListFromDB(){
        mRootDatabaseReference.child("FriendList").child(mProfileUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
                    String receiveUid = (String) dataSnapshot2.getValue();
                    mRootDatabaseReference.child("UserList").child(receiveUid).addListenerForSingleValueEvent(new ValueEventListener() { //상대 UID를 가지고 상대의 프로필 정보를 불러온다.
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG, "5");
                            Profile profile = dataSnapshot.getValue(Profile.class);
                            mFriendList.add(profile);
                            mFriendListAdapter.notifyItemInserted(mFriendList.size() - 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //친구요청 목록 불러오기
    private void loadFriendRequestListFromDB(){
        mRootDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("RequestFriend")){ //아예 디비조차 생성안된경우 ( 즉 친구없음)
                    Log.d(TAG, "1");
                }else{
                    mRootDatabaseReference.child("RequestFriend").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.hasChild(mProfileUid)){ //자신한테 지금까지 요청이 단 한번도 안 온 경우
                                Log.d(TAG, "2");
                            }else{ //친구요청이 있는 경우
                                mRootDatabaseReference.child("RequestFriend").child(mProfileUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.d(TAG, "3");
                                        for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) { //하위노드가 없을 떄까지 반복

                                            String receiveUid = (String) dataSnapshot2.getValue();
                                            Log.d(TAG, "4");
                                            Log.d(TAG, "친구요청온 UID : " + receiveUid);
                                            mRootDatabaseReference.child("UserList").child(receiveUid).addListenerForSingleValueEvent(new ValueEventListener() { //상대 UID를 가지고 상대의 프로필 정보를 불러온다.
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Log.d(TAG, "5");
                                                    Profile profile = dataSnapshot.getValue(Profile.class);
                                                    mReceiveFriendList.add(profile);
                                                    mReceiveFriendListAdapter.notifyItemInserted(mReceiveFriendList.size() - 1);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
