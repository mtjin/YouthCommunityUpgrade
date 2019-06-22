package com.mtjinse.myapplication.activity.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.adapters.ChatMessageAdapter;
import com.mtjinse.myapplication.activity.models.ChatMessage;
import com.mtjinse.myapplication.activity.models.Member;
import com.mtjinse.myapplication.activity.models.MyChatRoom;
import com.mtjinse.myapplication.activity.models.Profile;
import com.mtjinse.myapplication.activity.models.SendNotification;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

public class ChatRoomActivity extends AppCompatActivity {
    final static String TAG = "ChatRoomTAG";
    //xml
    private RecyclerView mMessagesRecyclerView;
    private EditText mWriteEditText;
    private Button mSendMessageButton;
    private Button mSendImageButton;
    //value
    private String mFriendUid = "";
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    private String mProfileNickName = "";
    private String mProfileImage = "";
    private ArrayList<ChatMessage> mChatMessageArrayList;
    private ChatMessageAdapter mChatMessageAdapter;
    private String mMessage = "";
    private String mChatRoomName;
    private Bitmap img = null; //비트맵 프로필사진
    private String mSendImageUrl;
    private String mFcmMessage; //fcm전송에 담을 메세지
    private String mFcmDate; // fcm전송에 담을 날짜
    private String mPushToken; //fcm전송을 위한 상대방 토큰
    Animation animClickSend; //댓글전송 버튼 클릭시 애니메이션
    AlertDialog alert;
    Boolean isExistChatRoom = false;
    Boolean isScrollDown;
    ValueEventListener valueEventListener;
    //날짜포맷
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private StorageReference mSendImageRef;
    //RequestCode
    final static int PICK_IMAGE = 1; //갤러리에서 사진선택

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//키보드가 UI가리는거 방지
        mMessagesRecyclerView = findViewById(R.id.chat_rev_messages);
        mWriteEditText = findViewById(R.id.chat_pt_write);
        mSendMessageButton = findViewById(R.id.chat_btn_send);
        mSendImageButton = findViewById(R.id.chat_btn_addphoto);
        loadProfileSharedPreferences();
        processIntent();
        animClickSend = AnimationUtils
                .loadAnimation(this, R.anim.send_scale); //댓글전송애니메이션

        mChatMessageArrayList = new ArrayList<>();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        //mMessagesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        mMessagesRecyclerView.setLayoutManager(layoutManager2); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
        //어댑터를 연결시켜준다.
        mChatMessageAdapter = new ChatMessageAdapter(mChatMessageArrayList, getApplicationContext());
        mMessagesRecyclerView.setAdapter(mChatMessageAdapter);

        loadMessageFromDB();
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickImageButton();
            }
        });
    }

    public void processIntent() {
        Intent intent = getIntent();
        mFriendUid = intent.getStringExtra("friendUid");
    }

    @Override
    protected void onStart() {
        super.onStart();
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatMessageArrayList.clear(); // 안해주면 데이터가 쌓임(중복
                mChatMessageAdapter.notifyDataSetChanged();
                for (DataSnapshot dataSnapshot3 : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = dataSnapshot3.getValue(ChatMessage.class);
                    chatMessage.setMessageUid(dataSnapshot3.getKey());
                    chatMessage.setChatRoomUid(mChatRoomName);
                    Log.d("FFFFFTTT", "챗룸에서 사진전송한 파일 : " + chatMessage.getSendImage());
                    mChatMessageArrayList.add(chatMessage);
                }
                mChatMessageAdapter.notifyDataSetChanged();
                mMessagesRecyclerView.scrollToPosition(mChatMessageArrayList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").addValueEventListener(valueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").removeEventListener(valueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //메세지 로드
    private void loadMessageFromDB() {
        mRootDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("ChatRoom")) { //첫 chatRoom 디비 생성인 경우
                    Member member = new Member(mProfileUid, mFriendUid);
                    mRootDatabaseReference.child("ChatRoom").push().child("Member").setValue(member); //채팅방생성
                    mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                mChatRoomName = dataSnapshot2.getKey();
                                Member member = dataSnapshot2.child("Member").getValue(Member.class);
                                Log.d(TAG, "멤버1 토큰11 : " + member.getuId1());
                                Log.d(TAG, "멤버2 토큰11 : " + member.getuId2());
                                if ((member.getuId1().equals(mProfileUid) || member.getuId2().equals(mProfileUid)) && (member.getuId1().equals(mFriendUid) || member.getuId2().equals(mFriendUid))) {
                                    mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").addValueEventListener(valueEventListener);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else { //
                    mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            isExistChatRoom = false; //이미 존재하는 방인지
                            for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                Member member = dataSnapshot2.child("Member").getValue(Member.class);
                                if ((member.getuId1().equals(mProfileUid) || member.getuId2().equals(mProfileUid)) && (member.getuId1().equals(mFriendUid) || member.getuId2().equals(mFriendUid))) { //원래있던 채팅방인 경우
                                    isExistChatRoom = true;
                                    mChatRoomName = dataSnapshot2.getKey(); //채팅방(push값)
                                    mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").addValueEventListener(valueEventListener);
                                }
                            }
                            if (!isExistChatRoom) {
                                Member member = new Member(mProfileUid, mFriendUid);
                                mRootDatabaseReference.child("ChatRoom").push().child("Member").setValue(member); //채팅방생성
                                mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                            mChatRoomName = dataSnapshot2.getKey();
                                            Member member = dataSnapshot2.child("Member").getValue(Member.class);
                                            Log.d(TAG, "멤버1 토큰22 : " + member.getuId1());
                                            Log.d(TAG, "멤버2 토큰22 : " + member.getuId2());
                                            if ((member.getuId1().equals(mProfileUid) || member.getuId2().equals(mProfileUid)) && (member.getuId1().equals(mFriendUid) || member.getuId2().equals(mFriendUid))) {
                                                mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").addValueEventListener(valueEventListener);
                                            }
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

    //메세지전송
    public void sendMessage() {
        mSendMessageButton.startAnimation(animClickSend);
        mMessage = mWriteEditText.getText().toString().trim();
        mFcmMessage = mWriteEditText.getText().toString().trim();//FCM을 위한 메세지를 따로 저장해놓는다.
        if (mMessage.equals("")) { //아무것도 안적은 경우

        } else {
            mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() { //친구가삭제한 경우는 메세지를 보내면안됨(친구삭제한경우 채팅방도 폭파됨). 채팅방이 존재하는지 확인
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(mChatRoomName)){
                        Calendar time = Calendar.getInstance();
                        String dates = format1.format(time.getTime());
                        mFcmDate = format1.format(time.getTime());//FCM을 위한 날짜를 따로 저장해놓는다.
                        sendGson(); //FCM 전송
                        ChatMessage chatMessage = new ChatMessage(mProfileUid, mProfileNickName, mProfileImage, mMessage, dates, "basic");
                        Log.d(TAG, "전송하는 채팅방이름 : " + mChatRoomName);
                        DatabaseReference messagePushKey = mRootDatabaseReference.push(); //푸시한 키값을 알아내기 위해 사용할것이다.
                        mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").child(messagePushKey.getKey()).setValue(chatMessage); //메세지 세팅
                        mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").child(messagePushKey.getKey()).child("ReadCount").child(mProfileUid).setValue(mProfileUid); //메세지본사람 등록
                        mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("RecentMessage").setValue(chatMessage);
                        mRootDatabaseReference.child("UserList").child(mFriendUid).addListenerForSingleValueEvent(new ValueEventListener() { //자기 정보에 자기가속한 채팅방 정보 남겨줌(채팅방이름과 상대닉네임)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Profile profile = dataSnapshot.getValue(Profile.class);
                                String friendNIckName = profile.getNickName();
                                String friendImage = profile.getProfileImage();
                                MyChatRoom myChatRoom = new MyChatRoom(mChatRoomName, friendNIckName, friendImage, mFriendUid);
                                mRootDatabaseReference.child("MyChatRoom").child(mProfileUid).child(mChatRoomName).setValue(myChatRoom);
                                MyChatRoom myChatRoom2 = new MyChatRoom(mChatRoomName, mProfileNickName, mProfileImage, mProfileUid);
                                mRootDatabaseReference.child("MyChatRoom").child(mFriendUid).child(mChatRoomName).setValue(myChatRoom2);
                                mChatMessageAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }else{
                        Toast.makeText(ChatRoomActivity.this, "친구삭제가 되어 채팅메세지를 보낼 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mWriteEditText.setText("");
    }

    //사진전송버튼 누른 경우
    private void clickImageButton() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE);
    }

    //사진전송
    private void sendImageMessage() {
        mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() { //친구가삭제한 경우는 메세지를 보내면안됨(친구삭제한경우 채팅방도 폭파됨). 채팅방이 존재하는지 확인
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mChatRoomName)){
                    if (img != null) { //기본이미지일 경우
                        mFcmMessage = mWriteEditText.getText().toString().trim(); //FCM을 위한 메세지를 따로 저장해놓는다.
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        img.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        byte[] datas = baos.toByteArray();
                        final String uniqueID = UUID.randomUUID().toString();
                        mSendImageRef = mStorageRef.child("ChatRoom").child(mChatRoomName);
                        UploadTask uploadTask = mSendImageRef.child(uniqueID).putBytes(datas);
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return mSendImageRef.child(uniqueID).getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    mSendImageUrl = String.valueOf(task.getResult());
                                    Calendar time = Calendar.getInstance();
                                    String dates = format1.format(time.getTime());
                                    mFcmDate = format1.format(time.getTime());//FCM을 위한 날짜를 따로 저장해놓는다.
                                    sendGson(); //FCM 전송
                                    ChatMessage chatMessage = new ChatMessage(mProfileUid, mProfileNickName, mProfileImage, "사진을 전송했습니다", dates, mSendImageUrl);
                                    Log.d(TAG, "전송하는 채팅방이름 : " + mChatRoomName);
                                    DatabaseReference messagePushKey = mRootDatabaseReference.push(); //푸시한 키값을 알아내기 위해 사용할것이다.
                                    mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").child(messagePushKey.getKey()).setValue(chatMessage); //메세지 세팅
                                    mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("Message").child(messagePushKey.getKey()).child("ReadCount").child(mProfileUid).setValue(mProfileUid); //메세지본사람 등록
                                    mRootDatabaseReference.child("ChatRoom").child(mChatRoomName).child("RecentMessage").setValue(chatMessage);
                                    mRootDatabaseReference.child("UserList").child(mFriendUid).addListenerForSingleValueEvent(new ValueEventListener() { //자기 정보에 자기가속한 채팅방 정보 남겨줌(채팅방이름과 상대닉네임)
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Profile profile = dataSnapshot.getValue(Profile.class);
                                            String friendNIckName = profile.getNickName();
                                            String friendImage = profile.getProfileImage();
                                            MyChatRoom myChatRoom = new MyChatRoom(mChatRoomName, friendNIckName, friendImage, mFriendUid);
                                            mRootDatabaseReference.child("MyChatRoom").child(mProfileUid).child(mChatRoomName).setValue(myChatRoom);
                                            MyChatRoom myChatRoom2 = new MyChatRoom(mChatRoomName, mProfileNickName, mProfileImage, mProfileUid);
                                            mRootDatabaseReference.child("MyChatRoom").child(mFriendUid).child(mChatRoomName).setValue(myChatRoom2);
                                            mChatMessageAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                }else{
                    Toast.makeText(ChatRoomActivity.this, "친구삭제가 되어 채팅메세지를 보낼 수 없습니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // 쉐어드값을 불러오는 메소드
    private void loadProfileSharedPreferences() {
        // mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();   //사용자 고유 토큰 받아옴
        SharedPreferences pref = getSharedPreferences(mProfileUid + "profile", MODE_PRIVATE);
        mProfileNickName = pref.getString("proNickName", "");
        if (pref.getString("proImage", "").equals("basic") || pref.getString("proImage", "").equals("")) { //사진 기본인 경우
            mProfileImage = "basic";
        } else { //기존사진데이터있는경우
            mProfileImage = pref.getString("proImage", "");
        }
    }

    /*
     * 카메라 및 사진 불러오기 관련
     * */
    @Override //갤러리에서 이미지 불러온 후 행동
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                img = BitmapFactory.decodeStream(in);
                in.close();
                sendImageMessage(); //사진 전송
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //노티알림
    private void sendGson() {
        mRootDatabaseReference.child("UserList").child(mFriendUid).child("ChatAlarm").child("chatAlarm").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String isChatAlarmOn = (String) dataSnapshot.getValue();
                Log.d(TAG, "상대방이 알림 on/off 여부 : " + isChatAlarmOn);
                if (isChatAlarmOn.equals("true")) { //알림설정을해줬으면 알림보낸다.
                    mRootDatabaseReference.child("UserList").child(mFriendUid).child("PushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, String> map = (Map<String, String>) dataSnapshot.getValue(); //상대유저의 토큰
                            mPushToken = map.get("pushToken");
                            if (mPushToken != null) {

                                Log.d(TAG, "상대방의 토큰 : " + mPushToken);
                                mRootDatabaseReference.child("UserList").child(mProfileUid).addListenerForSingleValueEvent(new ValueEventListener() { //내 닉네임담아서 보내주기위해
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Profile profile = dataSnapshot.getValue(Profile.class);
                                        SendNotification.sendNotification(mPushToken, profile.getNickName() + " (채팅방)", mFcmMessage);

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
