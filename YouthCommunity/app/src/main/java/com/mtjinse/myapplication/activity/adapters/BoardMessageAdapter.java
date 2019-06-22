package com.mtjinse.myapplication.activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.activities.FullBoardActivity;
import com.mtjinse.myapplication.activity.models.BoardMessage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BoardMessageAdapter extends RecyclerView.Adapter<BoardMessageAdapter.BoardMessageViewHolder> {
    static final String TAG = "BoardMessageAdapter";
    Context context;
    ArrayList<BoardMessage> items = new ArrayList<BoardMessage>();
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내  uid
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public BoardMessageAdapter(ArrayList<BoardMessage> items, Context context) {
        this.context = context;
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

    @NonNull
    @Override
    public BoardMessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_board_message, viewGroup, false);
        return new BoardMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BoardMessageViewHolder holder, int i) {
        final BoardMessage model = items.get(i);
        if(model != null) {
            holder.dateTextView.setText(model.getDates());
            holder.titleTextView.setText(model.getTitle());
            holder.nickNameTextView.setText(model.getNickName());
            if (model.getProfileImage().equals("basic")) { //프로필사진이 없는경우
                holder.profileCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
            } else {
                Glide.with(context).load(model.getProfileImage()).thumbnail(0.1f).placeholder(R.drawable.loading_spinner).into(holder.profileCircleImageView);
            }

            if (model.getMessageImage().equals("") || model.getMessageImage().equals("basic")) { //사진첨부안된 경우 이 뷰는 안보이게해준다.
                holder.hasImageTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.hasImageTextView.setVisibility(View.VISIBLE);
            }

            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FullBoardActivity.class);
                    intent.putExtra("FullBoardUid", model.getBoardUid()); //게시글 UID를 전달
                    intent.putExtra("FullBoardName", model.getBoardName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            //추천수
            mRootDatabaseReference.child("Board").child(model.getBoardName()).child(model.getBoardUid()).child("RecommendList").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int recommendSum = 0;
                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                        recommendSum += 1;
                    }
                    holder.recommendSumTextView.setText("" + recommendSum);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            Log.d(TAG, "onBindViewHolder model null");
        }
    }

    @Override
    public int getItemCount() {
        if(items == null){
            return  0;
        }else {
            return items.size();
        }
    }


    //뷰들을 바인딩 해줍니다.
    public class BoardMessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileCircleImageView;
        TextView nickNameTextView;
        TextView titleTextView;
        TextView dateTextView;
        TextView hasImageTextView; //사진이 있을때만 보이게함.
        TextView recommendSumTextView; //추천수
        LinearLayout linearLayout;

        public BoardMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            profileCircleImageView = itemView.findViewById(R.id.boardmessage_iv_photo);
            nickNameTextView = itemView.findViewById(R.id.boardmessage_tv_nickname);
            titleTextView = itemView.findViewById(R.id.boardmessage_tv_title);
            dateTextView = itemView.findViewById(R.id.boardmessage_tv_date);
            hasImageTextView = itemView.findViewById(R.id.boardmessage_tv_hasimage);
            recommendSumTextView = itemView.findViewById(R.id.boardmessage_tv_recommendSum);
            linearLayout = itemView.findViewById(R.id.boardmessage_linear);
        }
    }

    //아이템을 추가해주고싶을때 이거쓰면됨
    public void addItem(BoardMessage item) {
        items.add(item);
    }

    //한꺼번에 추가해주고싶을떄
    public void addItems(ArrayList<BoardMessage> items) {
        this.items = items;
    }

    //아이템 전부 삭제
    public void clear() {
        items.clear();
    }
}
