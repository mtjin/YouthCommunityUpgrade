package com.mtjinse.myapplication.activity.adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.mtjinse.myapplication.activity.activities.FullSNSActivity;
import com.mtjinse.myapplication.activity.activities.PhotoZoomInActivity;
import com.mtjinse.myapplication.activity.models.BoardMessage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SNSMessageAdapter extends RecyclerView.Adapter<SNSMessageAdapter.SNSMessageViewHolder> {
    final static String TAG = "SNSMessageAdapter";
    Context context;
    AppCompatActivity appCompatActivity;
    ArrayList<BoardMessage> items = new ArrayList<BoardMessage>();
    Animation animRecoomend; //추천애니메이션
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내  uid
    private String mProfileNickName;
    private String mDeleteBoardUid;
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public  SNSMessageAdapter(ArrayList<BoardMessage> items, Context context , AppCompatActivity appCompatActivity){
        this.context =  context;
        this.appCompatActivity = appCompatActivity;
        animRecoomend = AnimationUtils
                .loadAnimation(context, R.anim.recommend_scale); //추천애니메이션
        loadProfileSharedPreferences(); // 내 닉네임 불러와줌
        addItems(items);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override //어댑터에서 관리하는 아이템의 개수를 반환
    public int getItemCount() {
        if(items == null){
            return  0;
        }else {
            return items.size();
        }
    }

    //아이템을 추가해주고싶을때 이거쓰면됨
    public  void addItem(BoardMessage item){
        items.add(item);
    }

    //한꺼번에 추가해주고싶을떄
    public void addItems(ArrayList<BoardMessage> items){
        this.items = items;
    }

    //아이템 전부 삭제
    public void clear(){
        items.clear();
    }

    @NonNull
    @Override //뷰를 담을 수 있는 뷰홀더를 생성해줍니다. (이 뷰 홀더의 기준대로 리사이클러뷰가 만들어집니다.)
    public SNSMessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sns_message, viewGroup, false); //우리가쓸려는 chatMessage아이템의 뷰객체 생성
        return new SNSMessageViewHolder(view); //각각의 chatMessage아이템을 위한 뷰를 담고있는 뷰홀더객체를 반환한다.
    }

    @Override //홀더에 맞게 데이터들을 세팅해줍니다.
    public void onBindViewHolder(@NonNull final SNSMessageViewHolder holder, int i) {
        final BoardMessage model = items.get(i);
        if(model != null) {
            holder.titleTextVIew.setText(model.getTitle());
            if (model.getProfileImage().equals("basic")) { //프로필사진이 없는경우
                holder.profileImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
            } else {
                Glide.with(context).load(model.getProfileImage()).into(holder.profileImageView);
            }
            if (model.getMessageImage().equals("basic")) {
            } else {
                Glide.with(context).load(model.getMessageImage()).into(holder.uploadImageView);
            }

            holder.nickNameTextView.setText(model.getNickName());
            holder.dateTextView.setText(model.getDates());
            holder.messageTextView.setText(model.getMessage());

            //댓글개수 세팅
            mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild("Comment")) { //댓글이 없다면
                    } else {
                        mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).child("Comment").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int commentSum = 0;
                                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                    commentSum += 1;
                                }
                                holder.commentSumTextView.setText("" + commentSum);
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

            //좋아요개수 세팅
            mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild("Recommend")) { //추천하나도없을때

                    } else {
                        mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).child("Recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int recommendSum = 0;
                                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                    recommendSum += 1;
                                    if (dataSnapshot2.getKey().toString().equals(mProfileUid)) { //키로했는데 아닐수도있음
                                        holder.heartImageTextView.setBackgroundResource(R.drawable.ic_like);
                                    }
                                }
                                holder.recommendSumTextView.setText("" + recommendSum);
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

            //좋아요클릭
            holder.heartImageTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRootDatabaseReference.child("Board").child("SNS").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(model.getBoardUid())) { //이미삭제된 경우
                                Toast.makeText(context, "삭제된 게시물입니다", Toast.LENGTH_SHORT).show();
                            } else {
                                mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).child("Recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(mProfileUid)) { //이미추천누른경우(취소)

                                            mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).child("Recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    int recommendSum = 0;
                                                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                                        recommendSum += 1;
                                                    }
                                                    holder.recommendSumTextView.setText("" + (recommendSum - 1));
                                                    Toast.makeText(context, "하트 bye", Toast.LENGTH_SHORT).show();
                                                    mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).child("Recommend").child(mProfileUid).setValue(null);
                                                    holder.heartImageTextView.startAnimation(animRecoomend);
                                                    holder.heartImageTextView.setBackgroundResource(R.drawable.ic_dislike);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                            //  holder.recommendSumTextView.setText(model.getRecommend() - 1);
                                        } else { //좋아요누른 경우
                                            mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).child("Recommend").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    int recommendSum = 0;
                                                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                                        recommendSum += 1;
                                                    }
                                                    holder.recommendSumTextView.setText("" + (recommendSum + 1));
                                                    Toast.makeText(context, "하트 hi", Toast.LENGTH_SHORT).show();
                                                    mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).child("Recommend").child(mProfileUid).setValue(mProfileNickName);
                                                    holder.heartImageTextView.startAnimation(animRecoomend);
                                                    holder.heartImageTextView.setBackgroundResource(R.drawable.ic_like);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                            //holder.recommendSumTextView.setText(model.getRecommend() + 1);
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
            });

            //삭제버튼 클릭
            holder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRootDatabaseReference.child("Board").child("SNS").child(model.getBoardUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            BoardMessage boardMessage = dataSnapshot.getValue(BoardMessage.class);
                            String boardUid = boardMessage.getuId();
                            mDeleteBoardUid = model.getBoardUid();
                            if (boardUid.equals(mProfileUid)) {  //내가작성한 글이라면
                                deleteConfirmMessage();
                            } else {
                                Toast.makeText(context, "본인 외 게시물은 삭제가 불가합니다.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });


            //전체보기
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRootDatabaseReference.child("Board").child("SNS").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(model.getBoardUid())) { //게시물 삭제안된경우
                                Toast.makeText(context, "삭제된 게시물입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(context, FullSNSActivity.class);
                                intent.putExtra("SNSUid", model.getBoardUid());
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });

            //친구사진 누른경우
            holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PhotoZoomInActivity.class);
                    intent.putExtra("photoView", model.getProfileImage());
                    context.startActivity(intent);
                }
            });
        }else{
            Log.d(TAG, "onBindViewHolder model null");
        }
    }

    //뷰들을 바인딩 해줍니다.
    public class SNSMessageViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextVIew;
        CircleImageView profileImageView;
        TextView nickNameTextView;
        TextView dateTextView;
        TextView messageTextView;
        ImageView uploadImageView;
        TextView commentSumTextView;
        TextView recommendSumTextView;
        TextView heartImageTextView;
        ImageView deleteImageView;
        LinearLayout linearLayout;


        public SNSMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
           titleTextVIew = itemView.findViewById(R.id.snsitem_tv_title);
            profileImageView = itemView.findViewById(R.id.snsitem_iv_profileimage);
            nickNameTextView = itemView.findViewById(R.id.snsitem_tv_name);
            dateTextView = itemView.findViewById(R.id.snsitem_tv_date);
            messageTextView = itemView.findViewById(R.id.snsitem_tv_message);
            uploadImageView = itemView.findViewById(R.id.snsitem_iv_uploadimage);
            commentSumTextView = itemView.findViewById(R.id.snsitem_tv_commentsum);
            recommendSumTextView = itemView.findViewById(R.id.snsitem_tv_recommendsum);
            heartImageTextView = itemView.findViewById(R.id.snsitem_tv_heartimage);
            deleteImageView = itemView.findViewById(R.id.snsitem_iv_delete);
            linearLayout = itemView.findViewById(R.id.snsitem_linear);
        }
    }

    // 쉐어드값을 불러오는 메소드
    private void loadProfileSharedPreferences() {
        SharedPreferences pref = context.getSharedPreferences(mProfileUid+"profile", context.MODE_PRIVATE);
        mProfileNickName = pref.getString("proNickName", "");
    }

    public void deleteConfirmMessage() {

        //다이얼로그 객체 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
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
                Toast.makeText(context, "삭제되었습니다", Toast.LENGTH_SHORT).show();

                mRootDatabaseReference.child("Board").child("SNS").child(mDeleteBoardUid).setValue(null);
                appCompatActivity.finish();
            }
        });


        //예 버튼 눌렀을 때
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "취소됬습니다", Toast.LENGTH_SHORT).show();
            }
        });

        //만들어주기
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}