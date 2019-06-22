package com.mtjinse.myapplication.activity.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.models.BoardMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class WriteBoardActivity extends AppCompatActivity {
    //xml
    private Button mCancelButton;
    private Button mOkButton;
    private EditText mTitleEditText;
    private EditText mContentEditText;
    private ImageView mPhotoImageVIew;
    //value
    private String mProfileNickName;
    private String mProfileImage;
    private String mBoardName = ""; //게시판 이름
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    private String mTitle = "";
    private String mContent = "";
    private String mUploadImage = null; //업로드하는 사진
    private Bitmap img = null; //비트맵 프로필사진
    private String mCurrentPhotoPath; //카메라로 찍은 사진 저장할 루트경로
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();
    //파이어베이스 스토리지
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private StorageReference mBoardStroageRef;
    //RequestCode
    final static int PICK_IMAGE = 1; //갤러리에서 사진선택
    final static int CAPTURE_IMAGE = 2;  //카메라로찍은 사진선택
    //날짜포맷
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //로딩다이얼로그
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_board);

        mCancelButton = findViewById(R.id.writeboard_btn_cancel);
        mOkButton = findViewById(R.id.writeboard_btn_ok);
        mTitleEditText = findViewById(R.id.writeboard_et_title);
        mContentEditText = findViewById(R.id.writeboard_et_content);
        mPhotoImageVIew = findViewById(R.id.writeboard_iv_photo);
        processIntent();
        loadProfileSharedPreferences();
        saveWriteToDB();
        mPhotoImageVIew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoDialogRadio();
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessgeIsUpload(); //작성한거 업로드할건지 다일로그창
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessge(); //진짜 취소할건지 다일얼로그창
            }
        });
    }

    private void processIntent() {
        Intent intent = getIntent();
        mBoardName = intent.getStringExtra("board");

    }

    @Override
    protected void onStart() {
        super.onStart();
        mBoardStroageRef = mStorageRef.child("Board").child(mBoardName);
    }

    private void saveWriteToDB() {
        mTitle = mTitleEditText.getText().toString().trim();
        mContent = mContentEditText.getText().toString().trim();
        if (mTitle.equals("") || mContent.equals("")) { //공백이 있는 경우
            Toast.makeText(this, "빈칸이 있습니다.", Toast.LENGTH_SHORT).show();
        } else {
            try {
                loading();
                Calendar time = Calendar.getInstance();
                String dates = format1.format(time.getTime());
                if (img == null) { //업로드 사진있는경우
                    mUploadImage = "basic";
                    BoardMessage boardMessage = new BoardMessage(mProfileNickName, mProfileUid, mProfileImage, mUploadImage, mContent, dates, mTitle, 0);
                    mRootDatabaseReference.child("Board").child(mBoardName).push().setValue(boardMessage);
                    loadingEnd();
                    finish();
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] datas = baos.toByteArray();
                    final String uniqueID = UUID.randomUUID().toString();
                    UploadTask uploadTask = mBoardStroageRef.child(uniqueID).putBytes(datas);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            // Continue with the task to get the download URL
                            return mBoardStroageRef.child(uniqueID).getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                 @Override
                                                 public void onComplete(@NonNull Task<Uri> task) {
                                                     if (task.isSuccessful()) {
                                                         Calendar time = Calendar.getInstance();
                                                         String dates = format1.format(time.getTime());
                                                         mUploadImage = String.valueOf(task.getResult());
                                                         BoardMessage boardMessage = new BoardMessage(mProfileNickName, mProfileUid, mProfileImage, mUploadImage, mContent, dates, mTitle, 0);
                                                         mRootDatabaseReference.child("Board").child(mBoardName).push().setValue(boardMessage);
                                                         loadingEnd();
                                                         finish();
                                                     }
                                                 }
                                             }
                    );
                }
            } catch (Exception e) {
                Toast.makeText(this, "에러발생", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
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
                mPhotoImageVIew.setImageBitmap(img);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (resultCode == RESULT_OK) {
                try {
                    File file = new File(mCurrentPhotoPath);
                    InputStream in = getContentResolver().openInputStream(Uri.fromFile(file));
                    img = BitmapFactory.decodeStream(in);
                    mPhotoImageVIew.setImageBitmap(img);

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
                // Toast.makeText(ProfileActivity.this, PhotoModels[item] + "가 선택되었습니다.", Toast.LENGTH_SHORT).show();
                if (item == 0) { //갤러리
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE);
                } else if (item == 1) { //카메라찍은 사진가져오기
                    takePictureFromCameraIntent();
                } else { //기본화면으로하기
                    mPhotoImageVIew.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
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

    // 쉐어드값을 불러오는 메소드
    private void loadProfileSharedPreferences() {
        SharedPreferences pref = getSharedPreferences(mProfileUid + "profile", MODE_PRIVATE);
        //mEmail = pref.getString("email", "");
        mProfileNickName = pref.getString("proNickName", "");
        if (pref.getString("proImage", "").equals("basic") || pref.getString("proImage", "").equals("")) { //사진 기본인 경우
            mProfileImage = "basic";
        } else { //기존사진데이터있는경우
            mProfileImage = pref.getString("proImage", "");
        }
    }

    private void showMessge() {

        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //속성 지정
        builder.setTitle("안내");
        builder.setMessage("작성하시던 글이 지워집니다 " +
                "종료 하시겠습니까?");
        //아이콘
        builder.setIcon(android.R.drawable.ic_dialog_alert);


        //예 버튼 눌렀을 때
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(WriteBoardActivity.this, "예버튼이 눌렸습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        //예 버튼 눌렀을 때
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(WriteBoardActivity.this, "아니오 버튼이 눌렸습니다", Toast.LENGTH_SHORT).show();
            }
        });

        //만들어주기
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showMessgeIsUpload() {

        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //속성 지정
        builder.setTitle("게시글 작성");
        builder.setMessage("작성 하신 게시글을 적용하시겠습니까 ?");
        //아이콘
        builder.setIcon(android.R.drawable.ic_dialog_alert);


        //예 버튼 눌렀을 때
        builder.setPositiveButton("적용", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveWriteToDB();
            }
        });


        //예 버튼 눌렀을 때
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(WriteBoardActivity.this, "취소", Toast.LENGTH_SHORT).show();
            }
        });

        //만들어주기
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(WriteBoardActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("게시물 업로드 중입니다");
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

    //뒤로가기 2번 클릭시 종료

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로가기 버튼을 한번 더 누르면 뒤로가집니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
