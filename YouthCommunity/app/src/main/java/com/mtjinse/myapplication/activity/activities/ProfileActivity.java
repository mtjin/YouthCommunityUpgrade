package com.mtjinse.myapplication.activity.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.models.Profile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    static final String TAG = "ProfileTAG";
    //파이어베이스 스토리지
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private StorageReference mProfileRef;
    //파이어베이스 데이터베이스
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mUserListDatabaseReference;
    DatabaseReference mNickNameListDatabaseReference;

    //XML
    private CircleImageView mPhotoCircleImageView;
    private EditText mNickNameEditText;
    private EditText mIntroduceEditText;
    private Spinner mAgeSpinner;
    private Button mOkButton;

    //value
    ArrayList<String> ageArrayList;
    ArrayAdapter<String> ageAdapter;
    private String mProfileNickName;
    private String mProfileAge;
    private String mProfileImage;
    private String mProfileIntroduce;
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내  uid
    private String mProfileEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();//내  이메일
    private Bitmap img = null; //비트맵 프로필사진
    private int mTmpAge; //기존 나이스피너설정값 초기화에사용
    private String mTmpNickName; //닉네임 바꿀시 닉네임리스트디비에서원래이름 삭제하기위해 사용
    private String mCurrentPhotoPath; //카메라로 찍은 사진 저장할 루트경로
    private String mChatAlarmOn = "true";
    private String mBoardAlarmOn = "true";
    private long mLastClickTime = 0;
    //RequestCode
    final static int PICK_IMAGE = 1; //갤러리에서 사진선택
    final static int CAPTURE_IMAGE = 2;  //카메라로찍은 사진선택
    //로딩다이얼로그
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // 6.0 마쉬멜로우 이상일 경우에는 카메라 권한 체크 후 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        mPhotoCircleImageView = findViewById(R.id.profile_iv_photo);
        mNickNameEditText = findViewById(R.id.profile_pt_nickname);
        mIntroduceEditText = findViewById(R.id.profile_pt_introduce);
        mAgeSpinner = findViewById(R.id.profile_sp_age);
        mOkButton = findViewById(R.id.profile_btn_ok);

        //스피너 설정
        spinnerDo();
        loadProfileSharedPreferences();// 기존 정보불러오기
        loadInitalSetting(); //기존 데이터 세팅
        loadAlarmSharedPreferences();// 알람여부

        //프로필이미지 클릭 시
        mPhotoCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override //이미지 불러오기기
            public void onClick(View v) {
                photoDialogRadio(); //갤러리에서 불러오기 or 사진찍어서 불러오기
            }
        });
        //확인버튼 클릭시 닉네임 중복확인 후 디비에 저장
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileToDB();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mProfileRef = mStorageRef.child("profileImage").child(mProfileUid).child(mProfileUid + "image"); //프로필 스토리지 저장이름은 사용자 고유토큰과 "image"섞어 만든다.
        mUserListDatabaseReference = mRootDatabaseReference.child("UserList");
        mNickNameListDatabaseReference = mRootDatabaseReference.child("NickNameList");
        if (!mProfileNickName.equals("")) {
            autoPass();
        }
        // autoPass(); //자동로그인
        Log.d("AAAA", mProfileEmail);
        Log.d("AAAA", mProfileUid);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void saveProfileToDB() {
        //중복클릭방지
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        mProfileNickName = mNickNameEditText.getText().toString().trim();
        mProfileIntroduce = mIntroduceEditText.getText().toString().trim();
        if (!mProfileNickName.matches("[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힝]*")) {
            Toast.makeText(this, "특수문자는 불가합니다.", Toast.LENGTH_SHORT).show();
        } else {
            if (mProfileNickName.equals("") || mProfileIntroduce.equals("")) { //빈칸있을시s
                Toast.makeText(ProfileActivity.this, "빈칸을 모두 채워주세요", Toast.LENGTH_SHORT).show();
            } else {
                if (mProfileNickName.length() <= 1) {//아이디는 2글자 이상이여야한다.
                    Toast.makeText(this, "닉네임은 두글자 이상이여야 합니다.", Toast.LENGTH_SHORT).show();
                }else{
                loading();
                mRootDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("AAA", mProfileEmail);
                        Log.d("AAA", mProfileUid);
                        if (!dataSnapshot.hasChild("UserList")) { //처음 유저리스트 만드는 경우
                            if (img == null) { //이미지사진있는경우
                                mProfileImage = "basic";
                                Profile profile2 = new Profile(mProfileNickName, mProfileImage, mProfileEmail, mProfileAge, mProfileIntroduce, mProfileUid);
                                mUserListDatabaseReference.child(mProfileUid).setValue(profile2);
                                sendPushTokenToDB(); //fcm 토큰저장
                                mNickNameListDatabaseReference.child(mProfileNickName).setValue(mProfileUid);
                                saveProfileSharedPreferences(profile2);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                loadingEnd();
                                finish();
                            } else {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                img.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                byte[] datas = baos.toByteArray();
                                UploadTask uploadTask = mProfileRef.putBytes(datas);
                                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                        if (!task.isSuccessful()) {
                                            throw task.getException();
                                        }
                                        // Continue with the task to get the download URL
                                        return mProfileRef.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                             @Override
                                                             public void onComplete(@NonNull Task<Uri> task) {
                                                                 if (task.isSuccessful()) {
                                                                     mProfileImage = String.valueOf(task.getResult());
                                                                     Profile profile2 = new Profile(mProfileNickName, mProfileImage, mProfileEmail, mProfileAge, mProfileIntroduce, mProfileUid);
                                                                     mUserListDatabaseReference.child(mProfileUid).setValue(profile2);
                                                                     sendPushTokenToDB();
                                                                     mNickNameListDatabaseReference.child(mProfileNickName).setValue(mProfileUid);
                                                                     Log.d(TAG, "파이어스토어 이미지 업로드 및 프로필정보 디비 저장 완료");
                                                                     saveProfileSharedPreferences(profile2);
                                                                     Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                     startActivity(intent);
                                                                     loadingEnd();
                                                                     finish();
                                                                 }
                                                             }
                                                         }
                                );
                            }
                        } else {
                            mUserListDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Boolean isHasSameNIckName = false;
                                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                        Profile profile = dataSnapshot2.getValue(Profile.class);
                                        Log.d(TAG, "" + profile.getNickName().equals(mProfileNickName) + ",  " + !dataSnapshot2.getKey().equals(mProfileUid));
                                        Log.d(TAG, "" + profile.getNickName() + ",  " + dataSnapshot2.getKey());
                                        if (profile.getNickName().equals(mProfileNickName)) {  //닉네임중복일시
                                            isHasSameNIckName = true;
                                            if (!dataSnapshot2.getKey().equals(mProfileUid)) {
                                                Toast.makeText(ProfileActivity.this, "중복된 닉네임이 존재합니다", Toast.LENGTH_SHORT).show();
                                                loadingEnd();
                                            } else { //원래 자기 닉네임인경우
                                                if (img == null) { //기본이미지일 경우
                                                    mProfileImage = "basic"; //기본이미지인 경우는 basic으로 저장한다.
                                                    Profile profile2 = new Profile(mProfileNickName, mProfileImage, mProfileEmail, mProfileAge, mProfileIntroduce, mProfileUid);
                                                    mUserListDatabaseReference.setValue(profile2);
                                                    sendPushTokenToDB();
                                                    mNickNameListDatabaseReference.child(mProfileNickName).setValue(mProfileUid);
                                                    saveProfileSharedPreferences(profile2);
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                    Toast.makeText(ProfileActivity.this, "프로필변경이 완료되었습니다", Toast.LENGTH_SHORT).show();
                                                    loadingEnd();
                                                    finish();
                                                } else { //스토리지에 사진 올리고 url 받아옴
                                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                    img.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                                    byte[] datas = baos.toByteArray();
                                                    UploadTask uploadTask = mProfileRef.putBytes(datas);
                                                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                        @Override
                                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                            if (!task.isSuccessful()) {
                                                                throw task.getException();
                                                            }
                                                            // Continue with the task to get the download URL
                                                            return mProfileRef.getDownloadUrl();
                                                        }
                                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            if (task.isSuccessful()) {
                                                                mProfileImage = String.valueOf(task.getResult());
                                                                Profile profile2 = new Profile(mProfileNickName, mProfileImage, mProfileEmail, mProfileAge, mProfileIntroduce, mProfileUid);
                                                                mUserListDatabaseReference.child(mProfileUid).setValue(profile);
                                                                sendPushTokenToDB();
                                                                mNickNameListDatabaseReference.child(mProfileNickName).setValue(mProfileUid);
                                                                Log.d(TAG, "파이어스토어 이미지 업로드 및 프로필정보 디비 저장 완료");
                                                                saveProfileSharedPreferences(profile2);
                                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                startActivity(intent);
                                                                Toast.makeText(ProfileActivity.this, "프로필변경이 완료되었습니다", Toast.LENGTH_SHORT).show();
                                                                loadingEnd();
                                                                finish();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                    //중복된 닉네임이 없었을 경우 (원래 자기닉네임도 아님)
                                    if (!isHasSameNIckName) {
                                        if (img == null) { //기본이미지일 경우
                                            mProfileImage = "basic"; //기본이미지인 경우는 basic으로 저장한다.
                                            Profile profile2 = new Profile(mProfileNickName, mProfileImage, mProfileEmail, mProfileAge, mProfileIntroduce, mProfileUid);
                                            mUserListDatabaseReference.child(mProfileUid).setValue(profile2);
                                            sendPushTokenToDB(); //fcm 토큰저장
                                            if (!mTmpNickName.equals("")) {//기존닉네임 있었을시 디비에서삭제
                                                mNickNameListDatabaseReference.child(mTmpNickName).setValue(null);
                                            }
                                            mNickNameListDatabaseReference.child(mProfileNickName).setValue(mProfileUid);
                                            saveProfileSharedPreferences(profile2);
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent);
                                            Toast.makeText(ProfileActivity.this, "프로필변경이 완료되었습니다", Toast.LENGTH_SHORT).show();
                                            loadingEnd();
                                            finish();
                                        } else { //스토리지에 사진 올리고 url 받아옴
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            img.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                            byte[] datas = baos.toByteArray();
                                            UploadTask uploadTask = mProfileRef.putBytes(datas);
                                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                @Override
                                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                    if (!task.isSuccessful()) {
                                                        throw task.getException();
                                                    }
                                                    // Continue with the task to get the download URL
                                                    return mProfileRef.getDownloadUrl();
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if (task.isSuccessful()) {
                                                        mProfileImage = String.valueOf(task.getResult());
                                                        Profile profile2 = new Profile(mProfileNickName, mProfileImage, mProfileEmail, mProfileAge, mProfileIntroduce, mProfileUid);
                                                        mUserListDatabaseReference.child(mProfileUid).setValue(profile2);
                                                        sendPushTokenToDB(); //fcm 토큰저장
                                                        if (!mTmpNickName.equals("")) {//기존닉네임 있었을시 디비에서삭제
                                                            mNickNameListDatabaseReference.child(mTmpNickName).setValue(null);
                                                        }
                                                        mNickNameListDatabaseReference.child(mProfileNickName).setValue(mProfileUid);
                                                        Log.d(TAG, "파이어스토어 이미지 업로드 및 프로필정보 디비 저장 완료");
                                                        saveProfileSharedPreferences(profile2);
                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                        startActivity(intent);
                                                        Toast.makeText(ProfileActivity.this, "프로필변경이 완료되었습니다", Toast.LENGTH_SHORT).show();
                                                        loadingEnd();
                                                        finish();
                                                    }
                                                }
                                            });
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
        }
    }

    //스피너 세팅작업
    private void spinnerDo() {
        //스피너에 넣을 arrayList 데이터
        ageArrayList = new ArrayList<String>();
        ageArrayList.add("초1");
        ageArrayList.add("초2");
        ageArrayList.add("초3");
        ageArrayList.add("초4");
        ageArrayList.add("초5");
        ageArrayList.add("초6");
        ageArrayList.add("중1");
        ageArrayList.add("중2");
        ageArrayList.add("중3");
        ageArrayList.add("고1");
        ageArrayList.add("고2");
        ageArrayList.add("고3");
        ageArrayList.add("성인");


        mAgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mProfileAge = ageArrayList.get(position);
                mTmpAge = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //스피너어댑터 설정
        ageAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, ageArrayList);
        mAgeSpinner.setAdapter(ageAdapter);
    }

    @Override //갤러리에서 이미지 불러온 후 행동
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                img = BitmapFactory.decodeStream(in);
                in.close();
                // 이미지 표시
                mPhotoCircleImageView.setImageBitmap(img);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultCode == RESULT_OK) {
                try {
                    File file = new File(mCurrentPhotoPath);
                    InputStream in = getContentResolver().openInputStream(Uri.fromFile(file));
                    img = BitmapFactory.decodeStream(in);
                    mPhotoCircleImageView.setImageBitmap(img);

                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //사진찍기 or 앨범에서 가져오기 선택 다이얼로그
    private void photoDialogRadio() {
        final CharSequence[] PhotoModels = {"갤러리에서 가져오기", "카메라로 촬영 후 가져오기", "기본사진으로 하기"};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        //alt_bld.setIcon(R.drawable.icon);
        alt_bld.setTitle("프로필사진 설정");
        alt_bld.setSingleChoiceItems(PhotoModels, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) { //갤러리
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE);
                } else if (item == 1) { //카메라찍은 사진가져오기
                    takePictureFromCameraIntent();
                } else { //기본화면으로하기
                    mPhotoCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
                    img = null;
                }
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    //카메라 인텐트실행 함수
    private void takePictureFromCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mtjinse.myapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }

    //카메라로 촬영한 이미지를파일로 저장해주는 함수
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //로컬에 프로필정보 저장 (확인 버튼 클릭시 호출)
    private void saveProfileSharedPreferences(Profile profile) {
        Log.d(TAG, "프로필저장부분 " + mProfileUid);
        SharedPreferences pref = getSharedPreferences(mProfileUid + "profile", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("proEmail", profile.getEmail());
        editor.putString("proNickName", profile.getNickName());
        editor.putString("proAge", profile.getAge());
        editor.putString("proImage", profile.getProfileImage());
        editor.putString("proIntroduce", profile.getIntroduce());

        //스피너 초기값 세팅해줄때 사용
        editor.putInt("tmpAge", mTmpAge);
        Log.d(TAG, profile.getEmail());
        Log.d(TAG, profile.getNickName());
        Log.d(TAG, profile.getProfileImage());
        Log.d(TAG, profile.getAge());
        Log.d(TAG, profile.getIntroduce());
        editor.commit();
    }

    // 쉐어드값을 불러오는 메소드
    private void loadProfileSharedPreferences() {
        mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "프로필불러오기부분 " + mProfileUid);
        SharedPreferences pref = getSharedPreferences(mProfileUid + "profile", MODE_PRIVATE);
        //mEmail = pref.getString("email", "");
        mProfileNickName = pref.getString("proNickName", "");
        if (pref.getString("proImage", "").equals("basic") || pref.getString("proImage", "").equals("")) { //사진 기본인 경우
        } else { //기존사진데이터있는경우
            mProfileImage = pref.getString("proImage", "");
        }
        mProfileIntroduce = pref.getString("proIntroduce", "");
        mTmpAge = pref.getInt("tmpAge", 0);
        mTmpNickName = pref.getString("proNickName", "");
    }

    //이전에 했던 값들 미리 띄움
    private void loadInitalSetting() {
        mNickNameEditText.setText(mProfileNickName);
        mAgeSpinner.setSelection(mTmpAge, false);
        mIntroduceEditText.setText(mProfileIntroduce);
        if (mProfileImage != null) {
            Glide.with(ProfileActivity.this).load(mProfileImage).into(mPhotoCircleImageView);
        }
    }

    private void autoPass() {
        SharedPreferences pref = getSharedPreferences(mProfileUid + "profile", MODE_PRIVATE);
        Profile profile2 = new Profile(mProfileNickName, pref.getString("proImage", ""), mProfileEmail, pref.getString("proAge", ""), mProfileIntroduce, mProfileUid);
        mUserListDatabaseReference.child(mProfileUid).setValue(profile2);
        mNickNameListDatabaseReference.child(mProfileNickName).setValue(mProfileUid);
        sendPushTokenToDB(); //fcm 토큰저장

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        Toast.makeText(this, "자동로그인", Toast.LENGTH_SHORT).show();
    }

    private void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(ProfileActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("프로필 자동 불러오기 중입니다.");
                        progressDialog.show();
                    }
                }, 0);
    }

    private void loadingEnd() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 0);
    }

    private void sendPushTokenToDB() {
        //파이어베이스
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Map<String, Object> map = new HashMap<>();
                        map.put("pushToken", token);
                        mRootDatabaseReference.child("UserList").child(mProfileUid).child("PushToken").updateChildren(map);
                        sendChatAlarmOnToDB();
                        sendBoardAlarmOnToDB();
                        finish();
                    }
                });
    }

    private void sendChatAlarmOnToDB() {
        mRootDatabaseReference.child("UserList").child(mProfileUid).child("ChatAlarm").child("chatAlarm").setValue(mChatAlarmOn);
    }

    private void sendBoardAlarmOnToDB() {
        mRootDatabaseReference.child("UserList").child(mProfileUid).child("BoardAlarm").child("boardAlarm").setValue(mBoardAlarmOn);
    }

    private void loadAlarmSharedPreferences() {

        mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences pref = getSharedPreferences(mProfileUid + "Alarm", MODE_PRIVATE);
        Log.d(TAG, "채팅방알림 켰는지 : " + pref.getString("chatAlarm", ""));
        if (pref.getString("chatAlarm", "").equals("")) {

        } else {
            mChatAlarmOn = pref.getString("chatAlarm", "");
        }

        if (pref.getString("boardAlarm", "").equals("")) {

        } else {
            mBoardAlarmOn = pref.getString("boardAlarm", "");
        }
    }
}
