package com.mtjinse.myapplication.activity.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.adapters.BoardMessageAdapter;
import com.mtjinse.myapplication.activity.models.BoardMessage;

import java.util.ArrayList;

public class MainBoardActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    //xml
    private RecyclerView mBoardRecyclerView;
    private EditText mSearchEditText;
    private TextView mSearchTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //value
    private String mBoardName = "";
    private ArrayList<BoardMessage> mBoardMessageArrayList;
    private BoardMessageAdapter mBoardMessageAdapter;
    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton mFab, mWriteFab, mMyLikeFab, mBestFab;
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_board);
        mBoardRecyclerView = findViewById(R.id.mainboard_rev_mainboard);
        mSearchEditText = findViewById(R.id.mainboard_pt_search);
        mSearchTextView = findViewById(R.id.mainboard_tv_search);
        mFab = findViewById(R.id.mainboard_fab);
        mWriteFab = findViewById(R.id.mainboard_fab_write);
        mMyLikeFab = findViewById(R.id.mainboard_fab_mylike);
        mBestFab = findViewById(R.id.mainboard_fab_bestboard);
        //플로팅버튼 애니메이션
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        //리사이클러뷰 끝까지 끌어당기면 새로고침하게 해주는 뷰 onRefresh()에 해당코드 구현
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainboard_swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        processIntent();


        mSearchTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        mBoardMessageArrayList = new ArrayList<>();
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true); //레이아웃매니저 생성
        mBoardRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)); //아래구분선 세팅
        mBoardRecyclerView.setLayoutManager(layoutManager2); ////만든 레이아웃매니저 객체를(설정을) 리사이클러 뷰에 설정해줌
        //어댑터를 연결시켜준다.
        mBoardMessageAdapter = new BoardMessageAdapter(mBoardMessageArrayList, getApplicationContext());
        mBoardRecyclerView.setAdapter(mBoardMessageAdapter);

        //키보드 완료버튼누를시 검색
        mSearchEditText.setImeOptions(EditorInfo.IME_ACTION_DONE); // 키보드 확인 버튼 클릭시
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) { //처리할 일
                    if (mSearchEditText.getText().toString().trim().length() >= 2) {
                        search();
                        return true;
                    }
                }
                Toast.makeText(getApplicationContext(), "두글자 이상입력해야합니다", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mFab.setOnClickListener(this);
        mWriteFab.setOnClickListener(this);
        mMyLikeFab.setOnClickListener(this);
        mBestFab.setOnClickListener(this);

    }

    private void processIntent() {
        Intent intent = getIntent();
        mBoardName = intent.getStringExtra("board");
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadBoardFromDB(); //리아시클러뷰에 담을 게시글 목록들 불러오기
    }

    //리사이클러뷰에 담을 게시글 전부 불러오기
    private void loadBoardFromDB() {
        mRootDatabaseReference.child("Board").child(mBoardName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mBoardMessageArrayList.clear();
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    BoardMessage boardMessage = dataSnapshot2.getValue(BoardMessage.class);
                    boardMessage.setBoardUid(dataSnapshot2.getKey()); //게시글 uid 넣어줘서 삽입
                    boardMessage.setBoardName(mBoardName); //게시글 이름도 삽입
                    mBoardMessageArrayList.add(boardMessage);
                }
                mBoardMessageAdapter.notifyDataSetChanged();
                mBoardRecyclerView.scrollToPosition(mBoardMessageArrayList.size()-1); //스크롤 가장 위로 해준다.
                mSwipeRefreshLayout.setRefreshing(false);// refresh
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //내가 추천한 게시글 불러오기
    private void loadMyLikeBoardFromDB() {
        mRootDatabaseReference.child("Board").child(mBoardName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mBoardMessageArrayList.clear();
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    if(dataSnapshot2.child("Recommend").hasChild(mProfileUid)) { //내가추천한글 불러오기기
                       BoardMessage boardMessage = dataSnapshot2.getValue(BoardMessage.class);
                        boardMessage.setBoardUid(dataSnapshot2.getKey()); //게시글 uid 넣어줘서 삽입
                        boardMessage.setBoardName(mBoardName); //게시글 이름도 삽입
                        mBoardMessageArrayList.add(boardMessage);
                    }
                }
                mBoardMessageAdapter.notifyDataSetChanged();
                mBoardRecyclerView.scrollToPosition(mBoardMessageArrayList.size()-1); //스크롤 가장 위로 해준다.
                mSwipeRefreshLayout.setRefreshing(false);// refresh
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //추천수 10개이상인 best게시물만 불러오기
    private void loadBestBoardFromDB() {
        mRootDatabaseReference.child("Board").child(mBoardName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mBoardMessageArrayList.clear();
                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                    if(dataSnapshot2.child("Recommend").getChildrenCount() >= 10) { //내가추천한글 불러오기기
                        BoardMessage boardMessage = dataSnapshot2.getValue(BoardMessage.class);
                        boardMessage.setBoardUid(dataSnapshot2.getKey()); //게시글 uid 넣어줘서 삽입
                        boardMessage.setBoardName(mBoardName); //게시글 이름도 삽입
                        mBoardMessageArrayList.add(boardMessage);
                    }
                }
                mBoardMessageAdapter.notifyDataSetChanged();
                mBoardRecyclerView.scrollToPosition(mBoardMessageArrayList.size()-1); //스크롤 가장 위로 해준다.
                mSwipeRefreshLayout.setRefreshing(false);// refresh
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //글 검색
    private void search() {
        final String searchWord = mSearchEditText.getText().toString().toLowerCase().trim();
        if (searchWord.length() < 2) {
            Toast.makeText(this, "두글자 이상 입력해야합니다", Toast.LENGTH_SHORT).show();
        } else {
            mBoardMessageArrayList.clear();
            mBoardMessageAdapter.clear();
            mRootDatabaseReference.child("Board").child(mBoardName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        BoardMessage boardMessage = dataSnapshot2.getValue(BoardMessage.class);
                        String title = boardMessage.getTitle().toLowerCase().trim();
                        String message = boardMessage.getMessage().toLowerCase().trim();
                        String nickName = boardMessage.getNickName().toLowerCase().trim();
                        if (title.contains(searchWord) ||  message.contains(searchWord) || nickName.contains(searchWord)){
                            boardMessage.setBoardUid(dataSnapshot2.getKey()); //게시글 uid 넣어줘서 삽입
                            boardMessage.setBoardName(mBoardName); //게시글 이름도 삽입
                            mBoardMessageArrayList.add(boardMessage);
                        }
                    }
                    mBoardMessageAdapter.notifyDataSetChanged();
                    mBoardRecyclerView.scrollToPosition(mBoardMessageArrayList.size()-1); //스크롤 가장 위로 해준다.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onRefresh() {
        loadBoardFromDB();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.mainboard_fab:
                anim();
                break;
            case R.id.mainboard_fab_write:
                anim();
                Intent intent = new Intent(getApplicationContext(), WriteBoardActivity.class);
                intent.putExtra("board", mBoardName);
                Toast.makeText(this, "게시글 작성", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                break;
            case R.id.mainboard_fab_mylike:
                anim();
                loadMyLikeBoardFromDB();
                Toast.makeText(this, "내가 좋아요 한 게시글", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mainboard_fab_bestboard:
                anim();
                loadBestBoardFromDB();
                Toast.makeText(this, "추천수 10개 이상인 BEST 게시글", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void anim() {

        if (isFabOpen) {
            mWriteFab.startAnimation(fab_close);
            mMyLikeFab.startAnimation(fab_close);
            mBestFab.startAnimation(fab_close);
            mWriteFab.setClickable(false);
            mMyLikeFab.setClickable(false);
            mBestFab.setClickable(false);
            isFabOpen = false;
        } else {
            mWriteFab.startAnimation(fab_open);
            mMyLikeFab.startAnimation(fab_open);
            mBestFab.startAnimation(fab_open);
            mWriteFab.setClickable(true);
            mMyLikeFab.setClickable(true);
            mBestFab.setClickable(true);
            isFabOpen = true;
        }
    }
}
