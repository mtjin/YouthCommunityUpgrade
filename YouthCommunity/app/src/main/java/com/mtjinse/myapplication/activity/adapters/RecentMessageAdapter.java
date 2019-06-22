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
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.activities.ChatRoomActivity;
import com.mtjinse.myapplication.activity.models.ChatMessage;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecentMessageAdapter extends RecyclerView.Adapter<RecentMessageAdapter.RecentMessageViewHolder> {
    final static String TAG = "RecentMessageAdapter";
    Context context;
    ArrayList<ChatMessage> items = new ArrayList<ChatMessage>();


    public  RecentMessageAdapter(ArrayList<ChatMessage> items, Context context){
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

    @Override //어댑터에서 관리하는 아이템의 개수를 반환
    public int getItemCount() {
        if(items == null){
            return  0;
        }else {
            return items.size();
        }
    }

    //아이템을 추가해주고싶을때 이거쓰면됨
    public  void addItem(ChatMessage item){
        items.add(item);
    }

    //한꺼번에 추가해주고싶을떄
    public void addItems(ArrayList<ChatMessage> items){
        this.items = items;
    }

    //아이템 전부 삭제
    public void clear(){
        items.clear();
    }

    @NonNull
    @Override //뷰를 담을 수 있는 뷰홀더를 생성해줍니다. (이 뷰 홀더의 기준대로 리사이클러뷰가 만들어집니다.)
    public RecentMessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_tab1_recentchat, viewGroup, false); //우리가쓸려는 chatMessage아이템의 뷰객체 생성
        return new RecentMessageViewHolder(view); //각각의 chatMessage아이템을 위한 뷰를 담고있는 뷰홀더객체를 반환한다.
    }

    @Override //홀더에 맞게 데이터들을 세팅해줍니다.
    public void onBindViewHolder(@NonNull RecentMessageViewHolder holder, int i) {
         final ChatMessage model = items.get(i);
         if(model != null) {
             holder.senderTextView.setText(model.getNickName());
             if (model.getImage().equals("basic")) { //프로필사진이 없는경우
                 holder.profileCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
             } else {
                 Glide.with(context).load(model.getImage()).into(holder.profileCircleImageView);
             }
             holder.messageTextView.setText(model.getMessage());
             holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent intent = new Intent(context, ChatRoomActivity.class);
                     intent.putExtra("friendUid", model.getuId());
                     context.startActivity(intent);
                 }
             });

             //여기서만 date에 친구의 닉네임을 저장해줬음
             holder.nickNameTextView.setText(model.getDates());
         }else{
             Log.d(TAG, "onBindViewHolder model null");
         }
    }

    //뷰들을 바인딩 해줍니다.
    public class RecentMessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileCircleImageView;
        TextView nickNameTextView;
        TextView messageTextView;
        TextView senderTextView;
        LinearLayout linearLayout;

        public RecentMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            profileCircleImageView = itemView.findViewById(R.id.recentchat_iv_photo);
            nickNameTextView = itemView.findViewById(R.id.recentchat_tv_nickname);
            messageTextView = itemView.findViewById(R.id.recentchat_tv_message);
            senderTextView = itemView.findViewById(R.id.recentchat_tv_sender);
            linearLayout = itemView.findViewById(R.id.recentchat_linear);
        }
    }
}