package com.mtjinse.myapplication.activity.activities;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.models.Profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlarmDialogActivity extends AppCompatActivity {
    final static String TAG = "AlarmDialogActivityTAG";
    //xml
    private SwitchCompat mChatSwitch;
    private SwitchCompat mBoardSwitch;
    private Button mOkButton;
    //value
    private String mChatAlarmOn = "true";
    private String mBoardAlarmOn = "true";
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내  uid
    private String mProfileNickName;
    private String mProfileAge;
    private String mProfileImage;
    private String mProfileIntroduce;
    private String mProfileEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();//내  이메일
    //파이어베이스 데이터베이스
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_dialog);

        mOkButton = findViewById(R.id.alarm_btn_ok);
        mChatSwitch = findViewById(R.id.alarm_sw_chatalarm);
        mBoardSwitch = findViewById(R.id.alarm_sw_boardalarm);



        loadProfileSharedPreferences(); //프로필정보 불러오기
        loadInitalSetting(); //스위치값 세팅

        //채팅방알림 유무
        mChatSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                   @Override
                                                   public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                       if (isChecked) {
                                                           mChatAlarmOn = "true";
                                                           Log.d(TAG, "채팅방알림 ON 클릭");
                                                       } else {
                                                           mChatAlarmOn = "false";
                                                           Log.d(TAG, "채팅방알림 OFF 클릭");
                                                       }
                                                   }
                                               }

        );

        //게시물 알림 유무
        mBoardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBoardAlarmOn = "true";
                    Log.d(TAG, "게시판알림 ON 클릭");
                } else {
                    mBoardAlarmOn = "false";
                    Log.d(TAG, "게시판알림 OFF 클릭");
                }
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAlarmSharedPreferences(mChatAlarmOn, mBoardAlarmOn); //선택한거 저장
                sendPushTokenToServer(); //디비에 저장
            }
        });
    }

    //초기세팅
    private void loadInitalSetting() {
        mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences pref = getSharedPreferences(mProfileUid + "Alarm", MODE_PRIVATE);
        if(pref.getString("chatAlarm","").equals("")){
            mChatSwitch.setChecked(true);
        }else{
            mChatAlarmOn = pref.getString("chatAlarm","");
            Log.d(TAG, "채팅방알림 초깃값 세팅 :" + mChatAlarmOn);
            if(mChatAlarmOn.equals("true")){
                mChatSwitch.setChecked(true);
            }else{
                mChatSwitch.setChecked(false);
            }
        }

        if(pref.getString("boardAlarm","").equals("")){
            mBoardSwitch.setChecked(true);
        }else{
            mBoardAlarmOn = pref.getString("boardAlarm","");
            Log.d(TAG, "게시판알림 초깃값 세팅 :" + mBoardAlarmOn);
            if(mBoardAlarmOn.equals("true")){
                mBoardSwitch.setChecked(true);
            }else{
                mBoardSwitch.setChecked(false);
            }
        }
    }

    //Shard에 저장
    private void saveAlarmSharedPreferences(String chatAlarm, String boardAlarm) {
        mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences pref = getSharedPreferences(mProfileUid + "Alarm", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Log.d(TAG, "채팅방알림 쉐어드 저장 :" + mChatAlarmOn);
        Log.d(TAG, "게시판알림 쉐어드 저장 :" + mBoardAlarmOn);
        editor.putString("chatAlarm", mChatAlarmOn);
        editor.putString("boardAlarm", mBoardAlarmOn);
        editor.commit();
    }


    // 프로필 쉐어드값을 불러오는 메소드
    private void loadProfileSharedPreferences() {
        mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences pref = getSharedPreferences(mProfileUid + "profile", MODE_PRIVATE);
        //mEmail = pref.getString("email", "");
        mProfileNickName = pref.getString("proNickName", "");
        if (pref.getString("proImage", "").equals("basic") || pref.getString("proImage", "").equals("")) { //사진 기본인 경우
        } else { //기존사진데이터있는경우
            mProfileImage = pref.getString("proImage", "");
        }
        mProfileIntroduce = pref.getString("proIntroduce", "");
        mProfileAge = pref.getString("proAge", "");
        if (pref.getString("proImage", "").equals("basic") || pref.getString("proImage", "").equals("")) { //사진 기본인 경우
            mProfileImage = "basic";
        } else { //기존사진데이터있는경우
            mProfileImage = pref.getString("proImage", "");
        }
    }

    private void sendPushTokenToServer() {
        //파이어베이스
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        Profile profile2 = new Profile(mProfileNickName, mProfileImage, mProfileEmail, mProfileAge, mProfileIntroduce, mProfileUid);
                        mRootDatabaseReference.child("UserList").child(mProfileUid).setValue(profile2);

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Map<String, Object> map = new HashMap<>();
                        map.put("pushToken", token);
                        mRootDatabaseReference.child("UserList").child(mProfileUid).child("PushToken").updateChildren(map);
                        sendChatAlarmOnTokenToDB();
                        sendBoardAlarmOnToDB();
                        finish();
                    }
                });
    }

    private void sendChatAlarmOnTokenToDB() {
        mRootDatabaseReference.child("UserList").child(mProfileUid).child("ChatAlarm").child("chatAlarm").setValue(mChatAlarmOn);
    }

    private void sendBoardAlarmOnToDB() {
        mRootDatabaseReference.child("UserList").child(mProfileUid).child("BoardAlarm").child("boardAlarm").setValue(mBoardAlarmOn);
    }

}
