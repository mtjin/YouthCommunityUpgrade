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
import android.widget.TextView;
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
import com.mtjinse.myapplication.activity.adapters.OpenChatMessageAdapter;
import com.mtjinse.myapplication.activity.models.ChatMessage;
import com.mtjinse.myapplication.activity.models.MyChatRoom;
import com.mtjinse.myapplication.activity.models.Profile;
import com.mtjinse.myapplication.activity.models.SendNotification;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class OpenChatRoomActivity extends AppCompatActivity {
    final static String TAG = "OpenChatRoomActivityTAG";
    //xml
    private RecyclerView mMessagesRecyclerView;
    private EditText mWriteEditText;
    private Button mSendMessageButton;
    private TextView mTotalPeopleTextView;
    //value
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    private String mProfileNickName = "";
    private String mProfileImage = "";
    private ArrayList<ChatMessage> mOpenChatMessageArrayList;
    private OpenChatMessageAdapter mOpenChatMessageAdapter;
    private String mMessage = "";
    private String mChatRoomName = "";
    Animation animClickSend; //댓글전송 버튼 클릭시 애니메이션
    ValueEventListener valueEventListener; //메세지
    ValueEventListener valueEventListener2; //총인원수
    //날짜포맷
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_chat_room);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//키보드가 UI가리는거 방지
        mMessagesRecyclerView = findViewById(R.id.openchat_rev_messages);
        mWriteEditText = findViewById(R.id.openchat_pt_write);
        mSendMessageButton = findViewById(R.id.openchat_btn_send);
        mTotalPeopleTextView = findViewById(R.id.openchat_tv_total);

        processIntent();
        loadProfileSharedPreferences();
        animClickSend = AnimationUtils
                .loadAnimation(this, R.anim.send_scale); //댓글전송애니메이션

        mOpenChatMessageArrayList = new ArrayList<>();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        //mMessagesRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        mMessagesRecyclerView.setLayoutManager(layoutManager2); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
        //어댑터를 연결시켜준다.
        mOpenChatMessageAdapter = new OpenChatMessageAdapter(mOpenChatMessageArrayList, getApplicationContext());
        mMessagesRecyclerView.setAdapter(mOpenChatMessageAdapter);

        loadMessageFromDB();
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }


    private void processIntent() {
        Intent intent = getIntent();
        mChatRoomName = intent.getStringExtra("board");
        Log.d(TAG, "전달받은 채팅방이름 : " + mChatRoomName);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Calendar time2 = Calendar.getInstance(Locale.KOREA);
        String dates = format1.format(time2.getTime());
        Date enterTime = null;
        try {
            enterTime = format1.parse(dates);
        ; // 내가방에 들어온시간
        //메세지 감지
            Date finalEnterTime = enterTime;
            valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mOpenChatMessageArrayList.clear(); // 안해주면 데이터가 쌓임(중복
                mOpenChatMessageAdapter.notifyDataSetChanged();
                for (DataSnapshot dataSnapshot3 : dataSnapshot.getChildren()) {
                    ChatMessage chatMessage = dataSnapshot3.getValue(ChatMessage.class);
                    try {
                        Date date = format1.parse(chatMessage.getDates());
                        if(finalEnterTime.getTime() <= date.getTime()){ //내가 방에 들어온 이후 것 만 보여줌
                            chatMessage.setMessageUid(dataSnapshot3.getKey());
                            chatMessage.setChatRoomUid(mChatRoomName);
                            Log.d(TAG, "챗룸에서 사진전송한 파일 : " + chatMessage.getSendImage());
                            mOpenChatMessageArrayList.add(chatMessage);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.d(TAG , "DATE 에러");
                    }
                }
                mOpenChatMessageAdapter.notifyDataSetChanged();
                mMessagesRecyclerView.scrollToPosition(mOpenChatMessageArrayList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //총인원수 감지
        valueEventListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mTotalPeopleTextView.setText(dataSnapshot.getChildrenCount() + "명");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        //채팅방 들어왔다고 디비에 전송
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").child(mProfileUid).child(mProfileUid);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("Message").addValueEventListener(valueEventListener);
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").addValueEventListener(valueEventListener2);
        //채팅방 다시 들어왔다고 디비에 전송
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").child(mProfileUid).setValue(mProfileImage);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("Message").removeEventListener(valueEventListener);
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").removeEventListener(valueEventListener2);
        //채팅방 나갔다고 디비에 전송
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").child(mProfileUid).setValue(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //메세지 로드
    private void loadMessageFromDB() {
        Log.d(TAG, "메세지 불러오는 채팅방이름 : " + mChatRoomName);
        mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).addListenerForSingleValueEvent(new ValueEventListener() { //이거안해주면 데이터없는데 addValueEventListener 해주면면 npe뜬다.
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Message")){ // 이 카톡방의 첫 메세지가 아닌경우
                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("Message").addValueEventListener(valueEventListener);
                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").child(mProfileUid).setValue(mProfileUid);
                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").addValueEventListener(valueEventListener2);
                }else{ //카톡방에 대화가 한개도없는 경우
                    Calendar time = Calendar.getInstance(Locale.KOREA);
                    String dates = format1.format(time.getTime());
                    ChatMessage chatMessage = new ChatMessage("운영자UID", "운영자", "basic", "즐거운 대화나누세요 !!", dates, "basic");
                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("Message").push().setValue(chatMessage);
                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("Message").addValueEventListener(valueEventListener);

                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").child(mProfileUid).setValue(mProfileUid);
                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("TotalPeople").addValueEventListener(valueEventListener2);

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

        if (mMessage.equals("")) { //아무것도 안적은 경우

        } else {
            mRootDatabaseReference.child("ChatRoom").addListenerForSingleValueEvent(new ValueEventListener() { //친구가삭제한 경우는 메세지를 보내면안됨(친구삭제한경우 채팅방도 폭파됨). 채팅방이 존재하는지 확인
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //나라나 개인 설정시간이 다르면 같은시간대에 있어도 현재보내고있는 채팅을 볼 수 없으므로 한국으로 통일시켜서 저장해준다.
                    Calendar time = Calendar.getInstance(Locale.KOREA);
                    String dates = format1.format(time.getTime());

                    ChatMessage chatMessage = new ChatMessage(mProfileUid, mProfileNickName, mProfileImage, mMessage, dates, "basic");
                    mRootDatabaseReference.child("Board").child("OpenChatRoom").child(mChatRoomName).child("Message").push().setValue(chatMessage);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mWriteEditText.setText("");
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





}
