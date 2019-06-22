package com.mtjinse.myapplication.activity.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.mtjinse.myapplication.R;

public class PhotoZoomInActivity extends AppCompatActivity {

    PhotoView photoView;
    String photoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_zoom_in);
        photoView = findViewById(R.id.photoView);

        processIntent(); //인텐트 처리
    }

    private void  processIntent(){
        try {
            Intent intent = getIntent();
            photoUrl = intent.getStringExtra("photoView");
            if(photoUrl.equals("") || photoUrl.equals("basic")){
                photoView.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
            }else{
                Glide.with(this).load(photoUrl).into(photoView);
            }
        }catch (Exception e){
            Toast.makeText(this, "<오류> 없는 사진입니다", Toast.LENGTH_SHORT).show();
        }

    }
}
