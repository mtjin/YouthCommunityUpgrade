package com.mtjinse.myapplication.activity.activities;

import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.mtjinse.myapplication.R;
import com.mtjinse.myapplication.activity.fragments.Tab0Fragment;
import com.mtjinse.myapplication.activity.fragments.Tab1Fragment;
import com.mtjinse.myapplication.activity.fragments.Tab2Fragment;
import com.mtjinse.myapplication.activity.fragments.Tab3Fragment;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    //xml
    private ViewPager mViewPager;
    private ImageButton mTab1Button;
    private ImageButton mTab2Button;
    private ImageButton mTab3Button;
    private ImageButton mTab4Button;
    private ImageButton mAddFriendImageButton;
    private ImageButton mOptionImageButton;
    private TextView mToolbarLeftTitle;
    //value 뒤로가기관련
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //파이어베이스 크래쉬틱스 초기화
        Fabric.with(this, new Crashlytics());

        mViewPager = findViewById(R.id.main_viewpager);
        mTab1Button = findViewById(R.id.main_ibtn_first);
        mTab2Button = findViewById(R.id.main_ibtn_second);
        mTab3Button = findViewById(R.id.main_ibtn_third);
        mTab4Button = findViewById(R.id.main_ibtn_fourth);
        mAddFriendImageButton = findViewById(R.id.toolbar_addfriend);
        mOptionImageButton = findViewById(R.id.toolbar_option);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mToolbarLeftTitle = findViewById(R.id.toolbar_leftTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mViewPager.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        mViewPager.setCurrentItem(0);

        mTab1Button.setOnClickListener(movePageListener);
        mTab1Button.setTag(0);
        mTab2Button.setOnClickListener(movePageListener);
        mTab2Button.setTag(1);
        mTab3Button.setOnClickListener(movePageListener);
        mTab3Button.setTag(2);
        mTab4Button.setOnClickListener(movePageListener);
        mTab4Button.setTag(3);
        //초기 첫번쨰 탭 상단선 색 설정
        mTab1Button.setBackgroundResource(R.drawable.tab_button_upper_line);
        mTab1Button.setImageResource(R.drawable.ic_friends_list_black_24dp);
        mTab2Button.setImageResource(R.drawable.ic_message_black_no_selected_24dp);
        mTab3Button.setImageResource(R.drawable.ic_dashboard_no_selected_black_24dp);
        mTab4Button.setImageResource(R.drawable.ic_profile_no_selected_black_24dp);

        //친구추가버튼
        mAddFriendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddFriendDialogActivity.class);
                startActivity(intent);
            }
        });

        mOptionImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, mOptionImageButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.popup1) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            try {
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"yakumin112@naver.com"});

                                intent.setType("text/html");
                                intent.setPackage("com.google.android.gm");
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                intent.setType("text/html");
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"yakumin112@naver.com"});
                                startActivity(Intent.createChooser(intent, "Send Email"));
                            }
                            return true;
                        }else if(id == R.id.popup2){
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(getApplicationContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT);
                            Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                            //이 플래그는 API 11 (허니콤)부터 사용이가능한데 그 이하버전은 0.2%수준이다.
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            } else {
                                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            }
                            startActivity(intent2);
                            return true;
                        }
                        else if (id == R.id.popup3) {
                            Intent intent = new Intent(getApplicationContext(), AlarmDialogActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }

        });

        //스와이프할떄 툴바 이름변경하는데 사용
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }



            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mToolbarLeftTitle.setText(R.string.tab0_name);
                        mTab1Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                        mTab2Button.setBackgroundResource(R.color.white);
                        mTab3Button.setBackgroundResource(R.color.white);
                        mTab4Button.setBackgroundResource(R.color.white);
                        mTab1Button.setImageResource(R.drawable.ic_friends_list_black_24dp);
                        mTab2Button.setImageResource(R.drawable.ic_message_black_no_selected_24dp);
                        mTab3Button.setImageResource(R.drawable.ic_dashboard_no_selected_black_24dp);
                        mTab4Button.setImageResource(R.drawable.ic_profile_no_selected_black_24dp);
                        break;
                    case 1:
                        mToolbarLeftTitle.setText(R.string.tab1_name);
                        mTab2Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                        mTab1Button.setBackgroundResource(R.color.white);
                        mTab3Button.setBackgroundResource(R.color.white);
                        mTab4Button.setBackgroundResource(R.color.white);
                        mTab1Button.setImageResource(R.drawable.ic_friends_list_no_selected_black_24dp);
                        mTab2Button.setImageResource(R.drawable.ic_message_black_24dp);
                        mTab3Button.setImageResource(R.drawable.ic_dashboard_no_selected_black_24dp);
                        mTab4Button.setImageResource(R.drawable.ic_profile_no_selected_black_24dp);
                        break;
                    case 2:
                        mToolbarLeftTitle.setText(R.string.tab2_name);
                        mTab3Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                        mTab1Button.setBackgroundResource(R.color.white);
                        mTab2Button.setBackgroundResource(R.color.white);
                        mTab4Button.setBackgroundResource(R.color.white);
                        mTab1Button.setImageResource(R.drawable.ic_friends_list_no_selected_black_24dp);
                        mTab2Button.setImageResource(R.drawable.ic_message_black_no_selected_24dp);
                        mTab3Button.setImageResource(R.drawable.ic_dashboard_black_24dp);
                        mTab4Button.setImageResource(R.drawable.ic_profile_no_selected_black_24dp);
                        break;
                    case 3:
                        mToolbarLeftTitle.setText(R.string.tab3_name);
                        mTab4Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                        mTab1Button.setBackgroundResource(R.color.white);
                        mTab2Button.setBackgroundResource(R.color.white);
                        mTab3Button.setBackgroundResource(R.color.white);
                        mTab1Button.setImageResource(R.drawable.ic_friends_list_no_selected_black_24dp);
                        mTab2Button.setImageResource(R.drawable.ic_message_black_no_selected_24dp);
                        mTab3Button.setImageResource(R.drawable.ic_dashboard_no_selected_black_24dp);
                        mTab4Button.setImageResource(R.drawable.ic_profile_black_24dp);;
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    //탭버튼 클릭시 툴바이름 변경하는데 사용
    View.OnClickListener movePageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (int) v.getTag();
            if (tag == 0) {
                mToolbarLeftTitle.setText(R.string.tab0_name);
                mTab1Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                mTab2Button.setBackgroundResource(R.color.white);
                mTab3Button.setBackgroundResource(R.color.white);
                mTab4Button.setBackgroundResource(R.color.white);
                mTab1Button.setImageResource(R.drawable.ic_friends_list_black_24dp);
                mTab2Button.setImageResource(R.drawable.ic_message_black_no_selected_24dp);
                mTab3Button.setImageResource(R.drawable.ic_dashboard_no_selected_black_24dp);
                mTab4Button.setImageResource(R.drawable.ic_profile_no_selected_black_24dp);
            } else if (tag == 1) {
                mToolbarLeftTitle.setText(R.string.tab1_name);
                mTab2Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                mTab1Button.setBackgroundResource(R.color.white);
                mTab3Button.setBackgroundResource(R.color.white);
                mTab4Button.setBackgroundResource(R.color.white);
                mTab1Button.setImageResource(R.drawable.ic_friends_list_no_selected_black_24dp);
                mTab2Button.setImageResource(R.drawable.ic_message_black_24dp);
                mTab3Button.setImageResource(R.drawable.ic_dashboard_no_selected_black_24dp);
                mTab4Button.setImageResource(R.drawable.ic_profile_no_selected_black_24dp);
            } else if (tag == 2) {
                mToolbarLeftTitle.setText(R.string.tab2_name);
                mTab3Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                mTab1Button.setBackgroundResource(R.color.white);
                mTab2Button.setBackgroundResource(R.color.white);
                mTab4Button.setBackgroundResource(R.color.white);
                mTab1Button.setImageResource(R.drawable.ic_friends_list_no_selected_black_24dp);
                mTab2Button.setImageResource(R.drawable.ic_message_black_no_selected_24dp);
                mTab3Button.setImageResource(R.drawable.ic_dashboard_black_24dp);
                mTab4Button.setImageResource(R.drawable.ic_profile_no_selected_black_24dp);
            } else if (tag == 3) {
                mToolbarLeftTitle.setText(R.string.tab3_name);
                mTab4Button.setBackgroundResource(R.drawable.tab_button_upper_line);
                mTab1Button.setBackgroundResource(R.color.white);
                mTab2Button.setBackgroundResource(R.color.white);
                mTab3Button.setBackgroundResource(R.color.white);
                mTab1Button.setImageResource(R.drawable.ic_friends_list_no_selected_black_24dp);
                mTab2Button.setImageResource(R.drawable.ic_message_black_no_selected_24dp);
                mTab3Button.setImageResource(R.drawable.ic_dashboard_no_selected_black_24dp);
                mTab4Button.setImageResource(R.drawable.ic_profile_black_24dp);
            }
            mViewPager.setCurrentItem(tag);
        }
    };


    private class pagerAdapter extends FragmentStatePagerAdapter {
        public pagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Tab0Fragment();
                case 1:
                    return new Tab1Fragment();
                case 2:
                    return new Tab2Fragment();
                case 3:
                    return new Tab3Fragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    //뒤로가기 2번 클릭시 종료
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(), "뒤로가기 버튼을 한번 더 누르면 뒤로가집니다.", Toast.LENGTH_SHORT).show();
        }
    }


}
