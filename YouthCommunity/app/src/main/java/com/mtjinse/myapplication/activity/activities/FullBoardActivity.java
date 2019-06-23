package com.mtjinse.myapplication.activity.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.adapters.CommentAdapter;
import com.mtjinse.myapplication.activity.models.BoardMessage;
import com.mtjinse.myapplication.activity.models.Comment;
import com.mtjinse.myapplication.activity.models.SendNotification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class FullBoardActivity extends AppCompatActivity {
    static final String TAG = "FullBoardActivity";
    //xml
    private CircleImageView mProfileCircleImageView;
    private TextView mTitleTextView;
    private TextView mNickNameTextView;
    private TextView mDateTextView;
    private ImageView mUploadImageView;
    private TextView mContentTextView;
    private ImageView mRecommendImageView;
    private TextView mRecommentSumTextVIew;
    private RecyclerView mCommentRecyclerView;
    private EditText mCommentEditText;
    private Button mWriteCommentButton;
    private TextView mCommentSumTextView;
    private TextView mReviseTextView;
    private TextView mDeleteTextView;
    //value
    private String mBoardUid = "";
    private String mBoardName = "";
    private String mRelativeImage = ""; // 글쓴이 프로필사진
    private String mUploadImage = ""; //글쓴이가 업로드한사진
    int commentSum = 0;
    BoardMessage boardMessage;
    private ArrayList<Comment> mCommentArrayList;
    private CommentAdapter mCommentAdapter;
    private String mFcmMessage;
    Animation animRecoomend ;//추천애니메이션
    Animation animComment; //댓글전송 버튼 클릭시 애니메이션
    //내 프로필
    private String mProfileNickName = "";
    private String mProfileImage = "";
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    //날짜포맷
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();
    //RequestCode
    static final int REVISE_BOARD = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_board);
        mProfileCircleImageView = findViewById(R.id.fullboard_iv_photo);
        mTitleTextView = findViewById(R.id.fullboard_tv_title);
        mNickNameTextView = findViewById(R.id.fullboard_tv_nickname);
        mDateTextView = findViewById(R.id.fullboard_tv_date);
        mUploadImageView = findViewById(R.id.fullboard_iv_uploadimage);
        mContentTextView = findViewById(R.id.fullboard_tv_content);
        mRecommendImageView = findViewById(R.id.fullboard_iv_recommendup);
        mRecommentSumTextVIew = findViewById(R.id.fullboard_tv_recommend);
        mCommentRecyclerView = findViewById(R.id.fullboard_rev_comment);
        mCommentEditText = findViewById(R.id.fullboard_pt_inputcomment);
        mWriteCommentButton = findViewById(R.id.fullboard_btn_write);
        mCommentSumTextView = findViewById(R.id.fullboard_tv_commentsum);
        mReviseTextView = findViewById(R.id.fullboard_tv_revise);
        mDeleteTextView = findViewById(R.id.fullboard_tv_delete);
        //프로필정보 불러오기
        loadProfileSharedPreferences();
        //인텐트처리
        processIntent();
        //추천 애니메이션 세팅
        animRecoomend = AnimationUtils
                .loadAnimation(this, R.anim.recommend_scale);
        animComment = AnimationUtils
                .loadAnimation(this, R.anim.send_scale); //댓글전송애니메이션

        mCommentArrayList = new ArrayList<>();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false); //레이아웃매니저 생성
        mCommentRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        mCommentRecyclerView.setLayoutManager(layoutManager2); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
        //어댑터를 연결시켜준다.
        mCommentAdapter = new CommentAdapter(mCommentArrayList, getApplicationContext());
        mCommentRecyclerView.setAdapter(mCommentAdapter);
        //게시글불러오기
        loadBoardMessageFromDB();
        //댓글불러오기
        loadBoardCommentFromDB();
        //댓글작성이벤트
        mWriteCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCommentToDB();
            }
        });
        //게시물 수정이벤트
        mReviseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviseBoardMessage();
            }
        });
        //게시물 삭제이벤트
        mDeleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBoardMessage();
            }
        });
        //추천이벤트
        mRecommendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recommend();
            }
        });
        //글쓴이 사진클릭이벤트
        mProfileCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PhotoZoomInActivity.class);
                intent.putExtra("photoView", mRelativeImage);
                startActivity(intent);
            }
        });

        //게시물사진클릭이벤트
        mUploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PhotoZoomInActivity.class);
                intent.putExtra("photoView", mUploadImage);
                startActivity(intent);
            }
        });

    }

    private void processIntent() {
        Intent intent = getIntent();
        mBoardUid = intent.getStringExtra("FullBoardUid");
        mBoardName = intent.getStringExtra("FullBoardName");
    }

    //게시글 내용불러오기
    private void loadBoardMessageFromDB() {
        mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boardMessage = dataSnapshot.getValue(BoardMessage.class);
                mRelativeImage = boardMessage.getProfileImage();
                if (boardMessage.getProfileImage().equals("basic")) {
                    Glide.with(getApplicationContext()).load(R.drawable.com_facebook_profile_picture_blank_square).into(mProfileCircleImageView);
                } else {
                    Glide.with(getApplicationContext()).load(boardMessage.getProfileImage()).into(mProfileCircleImageView);
                }
                if (boardMessage.getMessageImage().equals("basic")) {
                } else {
                    Glide.with(getApplicationContext()).load(boardMessage.getMessageImage()).into(mUploadImageView);
                    mUploadImage = boardMessage.getMessageImage();
                }
                mTitleTextView.setText(boardMessage.getTitle());
                mNickNameTextView.setText(boardMessage.getNickName());
                mDateTextView.setText(boardMessage.getDates());
                mContentTextView.setText(boardMessage.getMessage());
                mRecommentSumTextVIew.setText(boardMessage.getRecommend() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //게시글 댓글 불러오기
    private void loadBoardCommentFromDB() {
        mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("Comment").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    Comment commentItem = dataSnapshot2.getValue(Comment.class);
                    mCommentArrayList.add(commentItem);
                    mCommentAdapter.notifyItemInserted(mCommentArrayList.size() - 1);
                }
                mCommentSumTextView.setText("" + mCommentArrayList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //댓글 저장
    private void saveCommentToDB() {
        mWriteCommentButton.startAnimation(animComment);
        final String comment = mCommentEditText.getText().toString().trim();
        if (comment.equals("")) {
            Toast.makeText(this, "공백입니다", Toast.LENGTH_SHORT).show();
        } else {
            mRootDatabaseReference.child("Board").child(mBoardName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(mBoardUid)) { //게시글 삭제된 경우
                        Toast.makeText(FullBoardActivity.this, "이미 삭제된 게시물입니다.", Toast.LENGTH_SHORT).show();
                    } else { //게시글 있는 경우
                        Calendar time = Calendar.getInstance();
                        String dates = format1.format(time.getTime());
                        Comment commentItem = new Comment(mProfileImage, mProfileNickName, dates, comment, mProfileUid);
                        mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("Comment").push().setValue(commentItem);
                        mCommentArrayList.add(commentItem);
                        mCommentAdapter.notifyDataSetChanged();
                        commentSum = Integer.parseInt(mCommentSumTextView.getText().toString());
                        commentSum += 1;
                        mCommentSumTextView.setText(commentSum+"");
                        Toast.makeText(FullBoardActivity.this, "댓글이 작성되었습니다.", Toast.LENGTH_SHORT).show();
                        //FCM 전송
                        mFcmMessage = comment;
                        sendGson();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mCommentEditText.setText("");

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

    //게시글 수정
    private void reviseBoardMessage() {
        if (boardMessage.getuId().equals(mProfileUid)) { //작성자 자신이 맞을 경우
            Intent intent = new Intent(getApplicationContext(), ReviseBoardActivity.class);
            intent.putExtra("ReviseBoardUid", mBoardUid); //게시글 UID를 전달
            intent.putExtra("ReviseBoardName", mBoardName);
            startActivityForResult(intent, REVISE_BOARD);
        } else {
            Toast.makeText(this, "자신의 게시글이 아니면 수정 할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //게시글 삭제
    private void deleteBoardMessage() {
        if (boardMessage.getuId().equals(mProfileUid)) { //작성자가 자신이 맞을 경우
            deleteConfirmMessage();
        } else {
            Toast.makeText(this, "자신의 게시글이 아니면 삭제 할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //게시글 추천
    private void recommend() {
        mRootDatabaseReference.child("Board").child(mBoardName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mBoardUid)) { //게시글 삭제된 경우
                    Toast.makeText(FullBoardActivity.this, "이미 삭제된 게시물입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final BoardMessage boardMessage = dataSnapshot.getValue(BoardMessage.class);

                            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("RecommendList").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(mProfileUid)) { //중복추천일시
                                        Toast.makeText(FullBoardActivity.this, "이미 추천을 했었습니다", Toast.LENGTH_SHORT).show();
                                    } else {
                                        int recommend = boardMessage.getRecommend();
                                        boardMessage.setRecommend(recommend + 1);
                                        mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("recommend").setValue(recommend + 1);
                                        mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("RecommendList").child(mProfileUid).setValue(mProfileUid);
                                        mRecommentSumTextVIew.setText(recommend + 1 + "");
                                        mRecommendImageView.startAnimation(animRecoomend);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REVISE_BOARD && resultCode == RESULT_OK){
            String ReviseResultTitle = data.getStringExtra("ReviseResultTitle");
            String ReviseResultMessage= data.getStringExtra("ReviseResultMessage");
            String ReviseResultUpLoadImage = data.getStringExtra("ReviseResultUpLoadImage");
            mTitleTextView.setText(ReviseResultTitle);
            mContentTextView.setText(ReviseResultMessage);
            if(ReviseResultUpLoadImage.equals("basic")){
                mUploadImageView.setImageResource(0);
            }else{
                Glide.with(getApplicationContext()).load(ReviseResultUpLoadImage).into(mUploadImageView);
            }
        }
    }

    private void deleteConfirmMessage() {

        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //속성 지정
        builder.setTitle("안내");
        builder.setMessage("게시글이 지워집니다 " +
                "정말 삭제 하시겠습니까?");
        //아이콘
        builder.setIcon(android.R.drawable.ic_dialog_alert);


        //예 버튼 눌렀을 때
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FullBoardActivity.this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).setValue(null);
                finish();
            }
        });


        //예 버튼 눌렀을 때
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FullBoardActivity.this, "취소됬습니다", Toast.LENGTH_SHORT).show();
            }
        });

        //만들어주기
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //FCM노티
    private void sendGson() {
        if (mFcmMessage.contains("#")) { //해쉬태그 당한 유저한테 알림이 가게함(댓글에 있는 사람이여야함)
            Pattern pattern = Pattern.compile("\\#([0-9a-zA-Z가-힣]*)");
            Matcher matcher = pattern.matcher(mFcmMessage);
            String extraHashTagName = ""; //해시태그한 유저 리스트
            matcher.find();
            extraHashTagName = matcher.group(1).replace("#", "");
            Log.d(TAG, "#해시태그한 단어 : " + extraHashTagName);

            //게시자에게 알림
            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    BoardMessage boardMessage = dataSnapshot.getValue(BoardMessage.class);
                    mRootDatabaseReference.child("UserList").child(boardMessage.getuId()).child("PushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, String> map = (Map<String, String>) dataSnapshot.getValue(); //상대유저의 토큰
                            String pushToken = map.get("pushToken");
                            if (pushToken != null) {
                                Log.d(TAG, "상대방의 토큰1 : " + pushToken);
                                SendNotification.sendNotification(pushToken, mProfileNickName + "(이)가 [" + mBoardName + ": " + boardMessage.getTitle() + "] 내 게시물에 댓글을 달았습니다","\n"+ mFcmMessage); //게시판이름과 제목도 같이 붙여서 보내준다
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            String finalExtraHashTagName = extraHashTagName;
            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("Comment").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        Comment comment = dataSnapshot2.getValue(Comment.class);
                        if (comment.getNickName().equals(finalExtraHashTagName)) {
                            mRootDatabaseReference.child("UserList").child(comment.getUid()).child("PushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    //동시에 노티를 보내면 안보내져서 이것은 0.5초뒤쯤 보내게로직구현
                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            //여기에 딜레이 후 시작할 작업들을 입력
                                            Map<String, String> map = (Map<String, String>) dataSnapshot.getValue(); //상대유저의 토큰
                                            String pushToken2 = map.get("pushToken");
                                            if (pushToken2 != null) {
                                                Log.d(TAG, "상대방의 토큰2 : " + pushToken2);
                                                SendNotification.sendNotification(pushToken2, mProfileNickName + "(이)가 [" + mBoardName + ": " + boardMessage.getTitle() + "] 게시물에 답댓글을 달았습니다", "\n"+mFcmMessage);
                                            }
                                        }
                                    }, 500);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
       /* i = 0;
        if (mFcmMessage.contains("#")) { //해쉬태그 당한 유저한테 알림이 가게함(댓글에 있는 사람이여야함)
            Pattern pattern = Pattern.compile("\\#([0-9a-zA-Z가-힣]*)");
            Matcher matcher = pattern.matcher(mFcmMessage);
            ArrayList<String> extraHashTagList = new ArrayList(); //해시태그한 유저 리스트
            //ArrayList<String> sendHashTagList = new ArrayList<>(); //보낼수있는 유저리스트목록
            while (matcher.find()) {
                Log.d(TAG, "#해시태그한 단어들 : " + matcher.group().replace("#", ""));
                if (!extraHashTagList.contains(matcher.group().replace("#", ""))) { //중복제거
                    extraHashTagList.add(matcher.group().replace("#", ""));
                }
            }
            i = 0;

            //게시자에게 알림
            mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    BoardMessage boardMessage = dataSnapshot.getValue(BoardMessage.class);
                    mRootDatabaseReference.child("UserList").child(boardMessage.getuId()).child("PushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, String> map = (Map<String, String>) dataSnapshot.getValue(); //상대유저의 토큰
                            String pushToken = map.get("pushToken");
                            if (pushToken != null) {
                                Log.d(TAG, "상대방의 토큰1 : " + pushToken);
                                SendNotification.sendNotification(pushToken, mProfileNickName + " [" + mBoardName + ": " + boardMessage.getTitle() + "]", mFcmMessage); //게시판이름과 제목도 같이 붙여서 보내준다
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            for (String extractHashTagName : extraHashTagList) {
                mRootDatabaseReference.child("Board").child(mBoardName).child(mBoardUid).child("Comment").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                            Comment comment = dataSnapshot2.getValue(Comment.class);
                            if (comment.getNickName().equals(extraHashTagList.get(i))) {
                                mRootDatabaseReference.child("UserList").child(comment.getUid()).child("PushToken").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Map<String, String> map = (Map<String, String>) dataSnapshot.getValue(); //상대유저의 토큰
                                        String pushToken2 = map.get("pushToken");
                                        if (pushToken2 != null) {
                                            Log.d(TAG, "상대방의 토큰2 : " + pushToken2);
                                            SendNotification.sendNotification(pushToken2, mProfileNickName + " [" + mBoardName + ": " + boardMessage.getTitle()+"]", mFcmMessage);
                                            Log.d(TAG, "2번. 댓글러 해시태그 비교 통과" + pushToken2);
                                            i++;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

        }*/
    }
}
