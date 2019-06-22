package com.mtjinse.myapplication.activity.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mtjinse.myapplication.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendReceiveActivity extends AppCompatActivity {
static final String TAG = "FriendReceiveTaG";
    //XML
    private CircleImageView mPhotoCircleImageView;
    private TextView mNickNameTextView;
    private TextView mIntroduceTextView;
    private TextView mAgeTextView;
    private Button mAcceptButton;
    private Button mRefuseButton;
    //value
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내  uid
    private String mRelativeProfileImage; //상대 프로필사진
    private String mRelativeNickName; //상대방닉네임
    private String mRelativeIntroduce;
    private String mRelativeAge;
    private String mRelativeUid;
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_receive);
        mPhotoCircleImageView = findViewById(R.id.friendreceive_iv_photo);
        mNickNameTextView = findViewById(R.id.friendreceive_tv_nickname);
        mIntroduceTextView = findViewById(R.id.friendreceive_tv_introduce);
        mAgeTextView = findViewById(R.id.friendreceive_tv_age);
        mAcceptButton = findViewById(R.id.friendreceive_btn_accept);
        mRefuseButton = findViewById(R.id.friendreceive_btn_refuse);
        processIntent();

        //친구수락버튼 클릭
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG , mProfileUid );
                mRootDatabaseReference.child("RequestFriend").child(mProfileUid).child(mRelativeUid).setValue(null); //친구요청에서는 일단 삭제
                //양쪽에 친구추가
                mRootDatabaseReference.child("FriendList").child(mProfileUid).child(mRelativeUid).setValue(mRelativeUid);
                mRootDatabaseReference.child("FriendList").child(mRelativeUid).child(mProfileUid).setValue(mProfileUid);
                finish();
            }
        });

        //친구신청 거절버튼 클릭
        mRefuseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //사용자 고유 토큰 받아옴
                mRootDatabaseReference.child("RequestFriend").child(mProfileUid).child(mRelativeUid).setValue(null);
                finish();
            }
        });

        //사진클릭이벤트
        mPhotoCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PhotoZoomInActivity.class);
                intent.putExtra("photoView", mRelativeProfileImage);
                startActivity(intent);
            }
        });
    }

    //인텐트처리
    public void processIntent(){
        Intent intent = getIntent();
        mRelativeProfileImage = intent.getStringExtra("profileImage");
        mRelativeNickName = intent.getStringExtra("profileNickName");
        mRelativeAge = intent.getStringExtra("profileAge");
        mRelativeIntroduce = intent.getStringExtra("profileIntroduce");
        mRelativeUid = intent.getStringExtra("profileUid");

        if(mRelativeProfileImage.equals("basic")){
            mPhotoCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
        }else{
            Glide.with(this).load(mRelativeProfileImage).into(mPhotoCircleImageView);
        }
        mNickNameTextView.setText(mRelativeNickName);
        mAgeTextView.setText(mRelativeAge);
        mIntroduceTextView.setText(mRelativeIntroduce);
    }

}
