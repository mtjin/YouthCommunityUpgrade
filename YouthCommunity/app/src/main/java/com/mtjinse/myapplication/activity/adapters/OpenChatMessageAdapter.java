package com.mtjinse.myapplication.activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.mtjinse.myapplication.activity.activities.PhotoZoomInActivity;
import com.mtjinse.myapplication.activity.models.ChatMessage;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

import de.hdodenhof.circleimageview.CircleImageView;

public class OpenChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    ArrayList<ChatMessage> items = new ArrayList<ChatMessage>();
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    final static String TAG = "ChatMessageAdapter";
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public OpenChatMessageAdapter(ArrayList<ChatMessage> items, Context context) {
        this.context = context;
        addItems(items);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                Log.d("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.d("FFFF", "온크리트뷰홀더 : 0인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_chatmessage2, viewGroup, false);
                return new ChatMessageViewHolder2(view);
            case 1:
                Log.d("FFFF", "온크리트뷰홀더 :" + viewType);
                Log.d("FFFF", "온크리트뷰홀더 : 1인 경우");
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_chatmessage, viewGroup, false);
                return new ChatMessageViewHolder(view);
        }
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_chatmessage, viewGroup, false);
        return new ChatMessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int i) {
        final ChatMessage model = items.get(i);
        if(model !=null) {
            Log.d("FFFFF", model.getNickName());
            Log.d("FFFFF", model.getMessage());
            Log.d("FFFFF", model.getDates());
            Log.d("FFFFF", model.getSendImage());
            if (model.getuId().equals(mProfileUid) && model.getSendImage().equals("basic")) { //내가보낸 글메세지
                final ChatMessageViewHolder2 holder2 = (ChatMessageViewHolder2) holder;
                Log.d(TAG, "나의메세지 + 글메세지");
                holder2.dateTextView2.setText(model.getDates());
                holder2.messageTextView2.setText(model.getMessage());

            } else if (!model.getuId().equals(mProfileUid) && model.getSendImage().equals("basic")) { //상대가보낸 글메세지
                final ChatMessageViewHolder holder1 = (ChatMessageViewHolder) holder;
                Log.d(TAG, "상대방의메세지 + 글메세지");
                holder1.nickNameTextView.setText(model.getNickName());
                if (model.getImage().equals("basic") || model.getImage().equals("")) { //프로필사진이 없는경우
                    holder1.photoCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
                } else {
                    Glide.with(context).load(model.getImage()).into(holder1.photoCircleImageView);
                }
                holder1.dateTextView.setText(model.getDates());
                holder1.messageTextView.setText(model.getMessage());
            }

        }else{
            Log.d(TAG, "onBindViewHolder model null");
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = items.get(position);
        Log.d(TAG, "겟아이템뷰타입 : " + chatMessage.getuId());
        Log.d(TAG, "겟아이템뷰타입 내 토큰 : " + mProfileUid);
        if (chatMessage.getuId().equals(mProfileUid) && chatMessage.getSendImage().equals("basic")) { //내 글메세지
            return 0;
        } else if (!chatMessage.getuId().equals(mProfileUid) && chatMessage.getSendImage().equals("basic")) { //상대 글메세지
            return 1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        if(items == null){
            return  0;
        }else {
            return items.size();
        }
    }


    //뷰들을 바인딩 해줍니다.  (상대방의 글메세지 채팅화면)
    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView photoCircleImageView;
        TextView nickNameTextView;
        TextView dateTextView;
        TextView messageTextView;
        TextView readCountTextVIew;
        LinearLayout linearLayout;

        public ChatMessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            photoCircleImageView = itemView.findViewById(R.id.chatmessage_iv_profile);
            nickNameTextView = itemView.findViewById(R.id.chatmessage_tv_nickname);
            dateTextView = itemView.findViewById(R.id.chatmessage_tv_date);
            messageTextView = itemView.findViewById(R.id.chatmessage_tv_message);
            readCountTextVIew = itemView.findViewById(R.id.chatmessage_tv_readcount);
            linearLayout = itemView.findViewById(R.id.chatmessage_item_linear);

            readCountTextVIew.setVisibility(View.INVISIBLE);
        }
    }

    //뷰들을 바인딩 해줍니다. (나의 글메세지 채팅화면)
    public class ChatMessageViewHolder2 extends RecyclerView.ViewHolder {

        TextView messageTextView2;
        TextView dateTextView2;
        TextView readCountTextVIew2;

        public ChatMessageViewHolder2(@NonNull final View itemView) {
            super(itemView);
            messageTextView2 = itemView.findViewById(R.id.chatmessage2_tv_message);
            dateTextView2 = itemView.findViewById(R.id.chatmessage2_tv_date);
            readCountTextVIew2 = itemView.findViewById(R.id.chatmessage2_tv_readcount);

            readCountTextVIew2.setVisibility(View.INVISIBLE);
        }
    }

    //아이템을 추가해주고싶을때 이거쓰면됨
    public void addItem(ChatMessage item) {
        items.add(item);
    }

    //한꺼번에 추가해주고싶을떄
    public void addItems(ArrayList<ChatMessage> items) {
        this.items = items;
    }

    //아이템 전부 삭제
    public void clear() {
        items.clear();
    }




}