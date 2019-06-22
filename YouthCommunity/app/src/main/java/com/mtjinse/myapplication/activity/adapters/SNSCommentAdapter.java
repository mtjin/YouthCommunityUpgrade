package com.mtjinse.myapplication.activity.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.activities.PhotoZoomInActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;

import com.mtjinse.myapplication.activity.models.Comment;

public class SNSCommentAdapter extends RecyclerView.Adapter<SNSCommentAdapter.SNSCommentViewHolder> {
    final static String TAG = "SNSCommentAdapter";
    Context context;
    ArrayList<Comment> items = new ArrayList<Comment>();

    public SNSCommentAdapter(ArrayList<Comment> items, Context context) {
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
    public SNSCommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_sns_comment, viewGroup, false);
        return new SNSCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SNSCommentViewHolder holder, int i) {
        final Comment model = items.get(i);
        if(model != null) {
            holder.dateTextView.setText(model.getDates());
            holder.commentTextView.setText(model.getComment());
            holder.nickNameTextView.setText(model.getNickName());
            if (model.getProfileImage().equals("basic")) { //프로필사진이 없는경우
                holder.profileCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
            } else {
                Glide.with(context).load(model.getProfileImage()).into(holder.profileCircleImageView);
            }

            //친구사진 누른경우
            holder.profileCircleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PhotoZoomInActivity.class);
                    intent.putExtra("photoView", model.getProfileImage());
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
    public class SNSCommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileCircleImageView;
        TextView nickNameTextView;
        TextView commentTextView;
        TextView dateTextView;

        public SNSCommentViewHolder(@NonNull final View itemView) {
            super(itemView);
            profileCircleImageView = itemView.findViewById(R.id.snscomment_iv_profile);
            nickNameTextView = itemView.findViewById(R.id.snscomment_tv_name);
            commentTextView = itemView.findViewById(R.id.snscomment_tv_comment);
            dateTextView = itemView.findViewById(R.id.snscomment_tv_date);
        }
    }

    //아이템을 추가해주고싶을때 이거쓰면됨
    public void addItem(Comment item) {
        items.add(item);
    }

    //한꺼번에 추가해주고싶을떄
    public void addItems(ArrayList<Comment> items) {
        this.items = items;
    }

    //아이템 전부 삭제
    public void clear() {
        items.clear();
    }
}
