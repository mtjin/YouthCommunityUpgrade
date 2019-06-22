package com.mtjinse.myapplication.activity.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mtjinse.myapplication.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendProfileActivity extends AppCompatActivity {
    //xml
    private CircleImageView mPhotoCircleImageView;
    private TextView mNickNameTextView;
    private TextView mIntroduceTextView;
    private TextView mAgeTextView;
    private CircleImageView mTalkICircleImageView;
    //value
    private String mFriendUid = ""; //친구 uid
    private String mFriendImage = "";
    private String mFriendAge = "";
    private String mFriendIntroduce = "";
    private String mFriendNickName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        mPhotoCircleImageView = findViewById(R.id.friendprofile_iv_photo);
        mNickNameTextView = findViewById(R.id.friendprofile_tv_nickname);
        mIntroduceTextView = findViewById(R.id.friendprofile_tv_introduce);
        mAgeTextView = findViewById(R.id.friendprofile_tv_age);
        mTalkICircleImageView = findViewById(R.id.friendprofile_iv_talk);
        processIntent(); //인텐트처리


        mTalkICircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , ChatRoomActivity.class);
                intent.putExtra("friendUid", mFriendUid);
                startActivity(intent);
                finish();
            }
        });

        //사진클릭이벤트
        mPhotoCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PhotoZoomInActivity.class);
                intent.putExtra("photoView", mFriendImage);
                startActivity(intent);
            }
        });
    }

    //인텐트처리
    public void processIntent() {
        Intent intent = getIntent();
        mFriendUid = intent.getStringExtra("friendUid");
        mFriendNickName = intent.getStringExtra("friendNickName");
        mFriendImage = intent.getStringExtra("friendImage");
        mFriendAge = intent.getStringExtra("friendAge");
        mFriendIntroduce = intent.getStringExtra("friendIntroduce");
        if (mFriendImage.equals("basic")) {
            mPhotoCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
        } else {
            Glide.with(this).load(mFriendImage).into(mPhotoCircleImageView);
        }
        mNickNameTextView.setText(mFriendNickName);
        mAgeTextView.setText(mFriendAge);
        mIntroduceTextView.setText(mFriendIntroduce);
    }
}
