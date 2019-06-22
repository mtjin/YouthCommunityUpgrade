package com.mtjinse.myapplication.activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mtjinse.myapplication.activity.activities.FriendReceiveActivity;
import com.mtjinse.myapplication.activity.activities.PhotoZoomInActivity;
import com.mtjinse.myapplication.activity.models.Profile;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReceiveFriendListAdapter extends RecyclerView.Adapter<ReceiveFriendListAdapter.ReceiveFriendListViewHolder> {
    final static String TAG = "ChatMessageAdapter";
    Context context;
    ArrayList<Profile> items = new ArrayList<Profile>();
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid();//내  uid
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public  ReceiveFriendListAdapter(ArrayList<Profile> items, Context context){
        this.context =  context;
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
    public ReceiveFriendListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_receivefriendlist, viewGroup, false);
        return new ReceiveFriendListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiveFriendListViewHolder holder, int i) {
        final Profile model = items.get(i);
        holder.nickNameTextView.setText(model.getNickName());
        if (model.getProfileImage().equals("basic")) { //프로필사진이 없는경우
            holder.photoCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
        } else {
            Glide.with(context).load(model.getProfileImage()).thumbnail(0.1f).placeholder(R.drawable.loading_spinner).into(holder.photoCircleImageView);
        }

        holder.itemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootDatabaseReference.child("RequestFriend").child(mProfileUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(model.getuId())){
                            Intent intent = new Intent(context, FriendReceiveActivity.class);
                            intent.putExtra("profileNickName", model.getNickName());
                            intent.putExtra("profileImage", model.getProfileImage());
                            intent.putExtra("profileAge", model.getAge());
                            intent.putExtra("profileIntroduce", model.getIntroduce());
                            intent.putExtra("profileUid", model.getuId());
                            context.startActivity(intent);
                        }else{ //이미수락되거나 거절당한 경우
                            Toast.makeText(context, "이미 수락하거나 거절한 친구입니다", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        //친구사진 누른경우
        holder.photoCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PhotoZoomInActivity.class);
                intent.putExtra("photoView", model.getProfileImage());
                context.startActivity(intent);
            }
        });
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
    public class ReceiveFriendListViewHolder extends RecyclerView.ViewHolder {
        CircleImageView photoCircleImageView;
        TextView nickNameTextView;
        LinearLayout itemLinearLayout;

        public ReceiveFriendListViewHolder(@NonNull final View itemView) {
            super(itemView);
            photoCircleImageView = itemView.findViewById(R.id.receivefriendlist_iv_photo);
            nickNameTextView = itemView.findViewById(R.id.receivefriendlist_tv_nickname);
            itemLinearLayout = itemView.findViewById(R.id.receivefriend_item_linear);
        }
    }
    //아이템을 추가해주고싶을때 이거쓰면됨
    public  void addItem(Profile item){
        items.add(item);
    }

    //한꺼번에 추가해주고싶을떄
    public void addItems(ArrayList<Profile> items){
        this.items = items;
    }

    //아이템 전부 삭제
    public void clear(){
        items.clear();
    }
}
