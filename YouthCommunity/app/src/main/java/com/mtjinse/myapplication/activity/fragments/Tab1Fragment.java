package com.mtjinse.myapplication.activity.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.adapters.ChatMessageAdapter;
import com.mtjinse.myapplication.activity.adapters.RecentMessageAdapter;
import com.mtjinse.myapplication.activity.models.ChatMessage;
import com.mtjinse.myapplication.activity.models.Member;
import com.mtjinse.myapplication.activity.models.MyChatRoom;
import com.mtjinse.myapplication.activity.models.Profile;

import java.util.ArrayList;


public class Tab1Fragment extends Fragment {
    ViewGroup rootView;
    //xml
    private RecyclerView mRecentChatRecyclerView;
    //value
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    String friendUid = "";
    String friendNIckName = "";
    int j = 0;
    private ArrayList<ChatMessage> mRecentMessageArrayList;
    private RecentMessageAdapter mRecentMessageAdapter;
    private ArrayList<MyChatRoom> mMyChatRoomArrayList;
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public Tab1Fragment() {
        // Required empty public const`ructor
    }

    @Override
    public void onStart() {
        super.onStart();
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mRecentMessageArrayList.clear();
        //최근메세지 데이터 불러오기
        loadRecentMessageFromDB();
        Log.d("SSS", "ONSTART");
    }

    @Override
    public void onResume() { //친구삭제한 경우
        super.onResume();
        Log.d("SSS", "onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("SSS", "onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("SSS", "onPause");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tab1, container, false);
        mRecentChatRecyclerView = rootView.findViewById(R.id.tab1_rev_chat);
        mRecentMessageArrayList = new ArrayList<>();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        mRecentChatRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        mRecentChatRecyclerView.setLayoutManager(layoutManager2); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
        //어댑터를 연결시켜준다.
        mRecentMessageAdapter = new RecentMessageAdapter(mRecentMessageArrayList, getActivity());
        mRecentChatRecyclerView.setAdapter(mRecentMessageAdapter);
     /*   //최근메세지 데이터 불러오기
        loadRecentMessageFromDB();*/
        Log.d("SSS", "OMCREATEBIEW");
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

    private void loadRecentMessageFromDB() {
        mRootDatabaseReference.child("MyChatRoom").child(mProfileUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMyChatRoomArrayList = new ArrayList<>();
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    mMyChatRoomArrayList.add(dataSnapshot2.getValue(MyChatRoom.class)); //내가 속해있는 채팅방 정보를(채팅방이름, 상대방닉네임, 상대방이미지) 리스트에 넣어준다.
                }
                for (int i = 0; i < mMyChatRoomArrayList.size(); i++) {
                    final MyChatRoom myChatRoom = mMyChatRoomArrayList.get(i);
                    mRootDatabaseReference.child("ChatRoom").child(mMyChatRoomArrayList.get(i).getChatRoom()).child("RecentMessage").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // mRecentMessageArrayList.clear();
                            ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                            for (int j = 0; j < mRecentMessageArrayList.size(); j++) {
                                if (mRecentMessageArrayList.get(j).getuId().equals(chatMessage.getuId())) { //만약 채팅을 치면 계쏙 업뎃을 해줘야하는데 기존 메세지는 삭제하고 새걸 넣어줘야한다.
                                    mRecentMessageArrayList.remove(j);
                                }
                            }
                            //  MyChatRoom myChatRoom = mMyChatRoomArrayList.get(i);
                            chatMessage.setImage(myChatRoom.getFriendImage());
                            chatMessage.setDates(myChatRoom.getFriendNickName());
                            chatMessage.setuId(myChatRoom.getFriendUid());
                            mRecentMessageArrayList.add(0,chatMessage);
                            mRecentMessageAdapter.notifyDataSetChanged();
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
   /* private void loadRecentMessageFromDB() {
        mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    Member member = dataSnapshot2.child("Member").getValue(Member.class);
                    if ((member.getuId1().equals(mProfileUid) || member.getuId2().equals(mProfileUid))) { //내 채팅방인 경우
                        if (member.getuId2().equals(mProfileUid)) {
                            friendUid = member.getuId1();
                        } else {
                            friendUid = member.getuId2();
                        }

                        mChatRoomName = dataSnapshot2.getKey(); //채팅방(push값)
                        if (dataSnapshot2.hasChild("RecentMessage")) { //최근메세지가 있다면
                            mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("RecentMessage").addValueEventListener(new ValueEventListener() { //해당채팅방 채팅내역을 불러옴
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    mRecentMessageAdapter.clear(); // 안해주면 데이터가 쌓임(중복
                                    chatMessage = dataSnapshot.getValue(ChatMessage.class);
                                    mRecentMessageArrayList.add(chatMessage);
                                    mRecentMessageAdapter.notifyDataSetChanged();
                                    *//*mRootDatabaseReference.child("UserList").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Profile profile = dataSnapshot.getValue(Profile.class);
                                            Log.d("TTTT", profile.getuId());
                                            Log.d("TTTT", profile.getNickName());
                                            Log.d("TTTT", profile.getProfileImage());
                                            chatMessage.setDates(profile.getNickName()); //날짜는 이부분에서는 안쓸거니깐 여기다가 친구의 닉네임을 적어준다.
                                            chatMessage.setImage(profile.getProfileImage());
                                            mRecentMessageArrayList.add(chatMessage);

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });*//*

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/

   /* private void loadRecentMessageFromDB() {
        mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    snapShotkey2 = dataSnapshot2.getKey();
                    mRootDatabaseReference.child("ChatRoom").child(snapShotkey2).child("Member").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Member member = dataSnapshot.getValue(Member.class);
                            Log.d("QQQQ", 4 + "");
                            if (member.getuId1().equals(mProfileUid) || member.getuId2().equals(mProfileUid)) {
                                if (member.getuId1().equals(mProfileUid)) { // 내 UID가 아닌 다른 UID는 친구의 UID
                                    friendUid = member.getuId2();
                                } else {
                                    friendUid = member.getuId1();
                                }
                                mRootDatabaseReference.child("ChatRoom").child(snapShotkey2).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.d("QQQQ", 3 + "");
                                        Log.d("QQQQ", "리센트메세지 내용나와야함 : " + dataSnapshot.getChildren().toString());
                                        //if (dataSnapshot.hasChild("RecentMessage")){
                                            mRootDatabaseReference.child("ChatRoom").child(snapShotkey2).child("RecentMessage").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Log.d("QQQQ", 2 + "");
                                                    recentMessage = dataSnapshot.getValue(ChatMessage.class);
                                                    if (recentMessage != null) {
                                                        recentMessage.setuId(friendUid);//친구 uid를 전달해주기위해
                                                        mRootDatabaseReference.child("UserList").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                Log.d("QQQQ", 1 + "");
                                                                Profile profile = dataSnapshot.getValue(Profile.class);
                                                                recentMessage.setDates(profile.getNickName()); //날짜는 이부분에서는 안쓸거니깐 여기다가 친구의 닉네임을 적어준다.
                                                                recentMessage.setImage(profile.getProfileImage());
                                                                mRecentMessageArrayList.add(recentMessage);
                                                                mRecentMessageAdapter.notifyItemInserted(mRecentMessageArrayList.size() - 1);
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
                                       // }
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
    }*/
   /* private void loadRecentMessageFromDB() {
        mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() { //채팅방 ChatRoom 디비노드에 접근
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    Member member = dataSnapshot2.child("Member").getValue(Member.class);  //Member Child에 있는 값 저장
                    if (member.getuId1().equals(mProfileUid) || member.getuId2().equals(mProfileUid)) { //멤버에 내 UID가 있는지 검사한다. ( 있으면 내가 속해있는 채팅방이다)
                        if (member.getuId1().equals(mProfileUid)) { // 내 UID가 아닌 다른 UID는 친구의 UID
                            friendUid = member.getuId2();
                        } else {
                            friendUid = member.getuId1();
                        }
                        Log.d("QQQQ0", "11111111111");
                        Log.d("QQQQ0", "유저1토큰 : " + member.getuId1() +" 유저2토큰 :" + member.getuId2());
                        Log.d("QQQQ1", dataSnapshot2.getKey());
                        Log.d("QQQQ2", dataSnapshot2.getValue() + "");
                         snapShotkey2 = dataSnapshot2.getKey();
                         Log.d("QQQQ", "스냅샷키2(채팅방이름)"+snapShotkey2);
                        mRootDatabaseReference.child("ChatRoom").child(dataSnapshot2.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild("RecentMessage")){
                                        Log.d("QQQQ", "스냅샷키2(채팅방이름) 2번째"+snapShotkey2);
                                        mRootDatabaseReference.child("ChatRoom").child(snapShotkey2).child("RecentMessage").addListenerForSingleValueEvent(new ValueEventListener() { //dataSnapshot2.getKey() : ChatRoom노드 뒤의 push값, 즉 방이름
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(friendUid != null) {
                                                    Log.d("QQQQ0", "222222222");
                                                    Log.d("QQQQ0", "에러나는곳에서의 친구 friendUid : " + friendUid);
                                                    Log.d("QQQQ0", "recentMEssage 닉네임: " + recentMessage.getNickName());
                                                    Log.d("QQQQ0", "recentMEssage UID: " + recentMessage.getuId());
                                                    recentMessage = dataSnapshot.getValue(ChatMessage.class);
                                                    //  Log.d("QQQQUID",recentMessage.getuId() );
                                                    recentMessage.setuId(friendUid);//친구 uid를 전달해주기위해
                                                    mRootDatabaseReference.child("UserList").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            Profile profile = dataSnapshot.getValue(Profile.class);
                                                            recentMessage.setDates(profile.getNickName()); //날짜는 이부분에서는 안쓸거니깐 여기다가 친구의 닉네임을 적어준다.
                                                            recentMessage.setImage(profile.getProfileImage());
                                                            mRecentMessageArrayList.add(recentMessage);
                                                            mRecentMessageAdapter.notifyItemInserted(mRecentMessageArrayList.size() - 1);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }*/
}
