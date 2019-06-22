package com.mtjinse.myapplication.activity.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.mtjinse.myapplication.activity.activities.FriendProfileActivity;
import com.mtjinse.myapplication.activity.activities.PhotoZoomInActivity;
import com.mtjinse.myapplication.activity.models.MyChatRoom;
import com.mtjinse.myapplication.activity.models.Profile;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder> {
    final static String TAG = "FriendListAdapter";
    //value
    private String mProfileUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //내 uid
    Context context;
    ArrayList<Profile> items = new ArrayList<Profile>();
    //DB
    DatabaseReference mRootDatabaseReference = FirebaseDatabase.getInstance().getReference();

    public  FriendListAdapter(ArrayList<Profile> items, Context context){
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
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_friendlist, viewGroup, false);
        return new FriendListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListViewHolder holder, int i) {
        final Profile model = items.get(i);
        if(model != null) {
            holder.nickNameTextView.setText(model.getNickName());
            if (model.getProfileImage().equals("basic")) { //프로필사진이 없는경우
                holder.photoCircleImageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
            } else {
                Glide.with(context).load(model.getProfileImage()).thumbnail(0.1f).placeholder(R.drawable.loading_spinner).into(holder.photoCircleImageView);
            }
            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FriendProfileActivity.class);
                    intent.putExtra("friendNickName", model.getNickName());
                    intent.putExtra("friendImage", model.getProfileImage());
                    intent.putExtra("friendAge", model.getAge());
                    intent.putExtra("friendIntroduce", model.getIntroduce());
                    intent.putExtra("friendUid", model.getuId());
                    context.startActivity(intent);
                }
            });

            holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final CharSequence[] items = {"친구 삭제"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("친구 삭제합니다. (한번삭제하면 되돌릴 수 없고 관련기록이 삭제됩니다..)");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item) {
                            mRootDatabaseReference.child("MyChatRoom").child(mProfileUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()) {
                                        MyChatRoom myChatRoom = dataSnapshot2.getValue(MyChatRoom.class);
                                        if (myChatRoom.getFriendUid().equals(model.getuId())) { //해당친구와 내가 속한 채팅방
                                            mRootDatabaseReference.child("MyChatRoom").child(mProfileUid).child(myChatRoom.getChatRoom()).setValue(null);
                                            mRootDatabaseReference.child("MyChatRoom").child(model.getuId()).child(myChatRoom.getChatRoom()).setValue(null);
                                            mRootDatabaseReference.child("FriendList").child(mProfileUid).child(model.getuId()).setValue(null);
                                            mRootDatabaseReference.child("FriendList").child(model.getuId()).child(mProfileUid).setValue(null);
                                            mRootDatabaseReference.child("ChatRoom").child(myChatRoom.getChatRoom()).setValue(null);
                                            Toast.makeText(context, "친구삭제되었습니다. 상대방측도 친구기록이 사라집니다", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                    builder.show();
                    return true;
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
    public class FriendListViewHolder extends RecyclerView.ViewHolder {
         CircleImageView photoCircleImageView;
         TextView nickNameTextView;
         LinearLayout linearLayout;

        public FriendListViewHolder(@NonNull final View itemView) {
            super(itemView);
           photoCircleImageView = itemView.findViewById(R.id.friendlist_iv_photo);
            nickNameTextView = itemView.findViewById(R.id.friendlist_tv_nickname);
            linearLayout = itemView.findViewById(R.id.firendlist_item_linear);
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
