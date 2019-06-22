package com.mtjinse.myapplication.activity.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Date;

public class ReviseBoardActivity extends AppCompatActivity {
    //xml
    private Button mCancelButton;
    private Button mOkButton;
    private EditText mTitleEditText;
    private EditText mContentEditText;
    private ImageView mPhotoImageVIew;
    //value
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    private String mBoardName;
    private String mBoardUid;
    private BoardMessage mBoardMessage;
    private String mInitalUploadImage = null; //초기 업로드했던사진
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
    //로딩다이얼로그
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revise_board);
        mCancelButton = findViewById(R.id.reviseboard_btn_cancel);
        mOkButton = findViewById(R.id.reviseboard_btn_ok);
        mTitleEditText = findViewById(R.id.reviseboard_et_title);
        mContentEditText = findViewById(R.id.reviseboard_et_content);
        mPhotoImageVIew = findViewById(R.id.reviseboard_iv_photo);
        processIntent(); //인텐트처리
        loadInitialFromDB(); //디비에서 원래 게시글 값 로드
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviseBoardMessage();
            }
        });

        mPhotoImageVIew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoDialogRadio();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBoardStroageRef = mStorageRef.child("Board").child(mBoardName);
    }

    private void processIntent() {
        Intent intent = getIntent();
        mBoardUid = intent.getStringExtra("ReviseBoardUid");
        mBoardName = intent.getStringExtra("ReviseBoardName");
        Log.d("BBBB", mBoardUid);
        Log.d("BBBB", mBoardName);
    }

    private void loadInitialFromDB() {
        mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mBoardMessage = dataSnapshot.getValue(BoardMessage.class);
                mTitleEditText.setText(mBoardMessage.getTitle());
                mContentEditText.setText(mBoardMessage.getMessage());
                if (mBoardMessage.getMessageImage().equals("basic")) { //업로드사진이없는 경우
                } else {
                    Glide.with(getApplicationContext()).load(mBoardMessage.getMessageImage()).into(mPhotoImageVIew);
                    mInitalUploadImage = mBoardMessage.getMessageImage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void reviseBoardMessage() {

        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //속성 지정
        builder.setTitle("안내");
        builder.setMessage("게시글이 수정됩니다 " +
                "수정 하시겠습니까?");
        //아이콘
        builder.setIcon(android.R.drawable.ic_dialog_alert);


        //예 버튼 눌렀을 때
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = mTitleEditText.getText().toString();
                String message = mContentEditText.getText().toString();
                if (title.equals("") || message.equals("")) { //공백이 있는 경우
                    Toast.makeText(getApplicationContext(), "빈칸이 있습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        loading();
                        Intent resultIntent = new Intent();

                        if (mInitalUploadImage != null) { //원래사진으로 한 경우
                            Toast.makeText(ReviseBoardActivity.this, "수정되었습니다", Toast.LENGTH_SHORT).show();
                            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("title").setValue(title);
                            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("message").setValue(message);
                            Toast.makeText(ReviseBoardActivity.this, "게시글이 수정되었습니다", Toast.LENGTH_SHORT).show();
                            resultIntent.putExtra("ReviseResultTitle", title);
                            resultIntent.putExtra("ReviseResultMessage", message);
                            resultIntent.putExtra("ReviseResultUpLoadImage", mInitalUploadImage);
                            loadingEnd();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else if (img == null) { //업로드 사진 없앤  경우
                            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("title").setValue(title);
                            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("message").setValue(message);
                            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("messageImage").setValue("basic");
                            Toast.makeText(ReviseBoardActivity.this, "게시글이 수정되었습니다", Toast.LENGTH_SHORT).show();
                            resultIntent.putExtra("ReviseResultTitle", title);
                            resultIntent.putExtra("ReviseResultMessage", message);
                            resultIntent.putExtra("ReviseResultUpLoadImage", "basic");
                            loadingEnd();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        } else { //업로드사진 수정한 경우
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            img.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                            byte[] datas = baos.toByteArray();
                            UploadTask uploadTask = mBoardStroageRef.putBytes(datas);
                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    // Continue with the task to get the download URL
                                    return mBoardStroageRef.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                         @Override
                                                         public void onComplete(@NonNull Task<Uri> task) {
                                                             if (task.isSuccessful()) {

                                                                 String mUploadImage = String.valueOf(task.getResult());
                                                                 String title = mTitleEditText.getText().toString();
                                                                 String message = mContentEditText.getText().toString();
                                                                 mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("title").setValue(title);
                                                                 mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("message").setValue(message);
                                                                 mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("messageImage").setValue(mUploadImage);
                                                                 Toast.makeText(ReviseBoardActivity.this, "게시글이 수정되었습니다", Toast.LENGTH_SHORT).show();
                                                                 Intent resultIntent = new Intent();
                                                                 resultIntent.putExtra("ReviseResultTitle", title);
                                                                 resultIntent.putExtra("ReviseResultMessage", message);
                                                                 resultIntent.putExtra("ReviseResultUpLoadImage", mUploadImage);
                                                                 loadingEnd();
                                                                 setResult(RESULT_OK, resultIntent);
                                                                 finish();
                                                             }
                                                         }
                                                     }
                            );
                        }
                    } catch (Exception e) {
                        Toast.makeText(ReviseBoardActivity.this, "에러발생", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });


        //아니요 버튼 눌렀을 때
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ReviseBoardActivity.this, "취소됬습니다", Toast.LENGTH_SHORT).show();
            }
        });

        //만들어주기
        AlertDialog dialog = builder.create();
        dialog.show();
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
                mInitalUploadImage = null;
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
                    mInitalUploadImage = null;
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
                    img = null;
                    mInitalUploadImage = null;
                    mPhotoImageVIew.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
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

    private void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(ReviseBoardActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("게시물 수정 업로드 중입니다");
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
