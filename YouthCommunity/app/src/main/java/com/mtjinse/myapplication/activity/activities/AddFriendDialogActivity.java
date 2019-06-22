package com.mtjinse.myapplication.activity.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mtjinse.myapplication.R;

public class AddFriendDialogActivity extends AppCompatActivity { //친구추가하는 다이얼로그창
    final static String TAG = "DialogTAG";
    //xml
    private EditText mFindFriendEditText;
    private Button mOkButton;
    //value
    private String mFindName = ""; //추가할 유저의 닉네임
    private String mFindUid = ""; //추가할 유저의 UID
    private String mProfileNickName; // 내 닉네임
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내  uid
    Intent intent;
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_dialog);
        mFindFriendEditText = findViewById(R.id.dialog_pt_nickname);
        mOkButton = findViewById(R.id.dialog_btn_ok);
        loadProfileSharedPreferences(); //내 프로필 정보 로딩

        //친구추가신청
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFindName = mFindFriendEditText.getText().toString().trim();
                if (mFindName.equals("")) {
                    Toast.makeText(AddFriendDialogActivity.this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (mFindName.equals(mProfileNickName)) {
                    Toast.makeText(AddFriendDialogActivity.this, "자기자신에게는 친구추가를 할 수 없습니다", Toast.LENGTH_SHORT).show();
                } else {
                    showMessge();
                }
            }
        });
    }

    public void showMessge() {

        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //속성 지정
        builder.setTitle("안내");
        builder.setMessage(mFindName + " 님께 친구신청 하시겠씁니까?");
        //아이콘
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        //예 버튼 눌렀을 때
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                yesButton();
            }
        });


        //예 버튼 눌렀을 때
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        //만들어주기
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 쉐어드값을 불러오는 메소드
    private void loadProfileSharedPreferences() {
        SharedPreferences pref = getSharedPreferences(mProfileUid + "profile", MODE_PRIVATE);
        mProfileNickName = pref.getString("proNickName", "");
    }

    public void yesButton(){
        mRootDatabaseReference.child("NickNameList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mFindName)) { //해당 닉네임이 존재하는 경우
                    mFindUid = (String) dataSnapshot.child(mFindName).getValue();
                    Log.d(TAG, "찾은 상대방의 UID : " + mFindUid);
                    mRootDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() { //디비 처음만들어지는건지 검사
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("FriendList").child(mProfileUid).hasChild(mFindUid)) { //이미친구인 경우
                                Toast.makeText(AddFriendDialogActivity.this, "이미 친구입니다 ^_^", Toast.LENGTH_SHORT).show();
                            } else {
                                if (dataSnapshot.child("RequestFriend").child(mProfileUid).hasChild(mFindUid)) { //해당 유저한테 친구신청와있는 경우
                                    Toast.makeText(AddFriendDialogActivity.this, "친구요청이 와있습니다", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (!dataSnapshot.hasChild("RequestFriend")) {
                                        mRootDatabaseReference.child("RequestFriend").child(mFindUid).child(mProfileUid).setValue(mProfileUid);
                                        Toast.makeText(AddFriendDialogActivity.this, "친구신청했습니다. 상대박이 수락하면 친구가 됩니다.", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "1");
                                    } else { //RequestFriend 디비노드 첫생성시 아닌경우(보통의 경우)
                                        mRootDatabaseReference.child("RequestFriend").child(mFindUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild(mProfileUid)) { //이미 친구신청 한 경우
                                                    Toast.makeText(AddFriendDialogActivity.this, "이미 친구신청을 보낸상태입니다", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "2");
                                                } else {
                                                    mRootDatabaseReference.child("RequestFriend").child(mFindUid).child(mProfileUid).setValue(mProfileUid);
                                                    Toast.makeText(AddFriendDialogActivity.this, "친구신청했습니다. 상대박이 수락하면 친구가 됩니다.", Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "3");
                                                }
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
                } else {
                    Toast.makeText(AddFriendDialogActivity.this, "해당 닉네임은 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
