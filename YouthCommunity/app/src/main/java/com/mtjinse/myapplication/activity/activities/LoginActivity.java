package com.mtjinse.myapplication.activity.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mtjinse.myapplication.R;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener { //GoogleApiClient.OnConnectionFailedListener, View.OnClickListener 구현
    final static String TAG = "LoginActivityTAG";

    private EditText mIdEditText;
    private EditText mPasswordEditText;
    private CheckBox mCheckBox;
    private String mEmail;
    private String mPassword;
    private boolean isSavedLogData;
    private SharedPreferences mAppData;

    // 구글로그인 result 상수
    private static final int CODE_SIGN_IN = 1000;
    // 구글api클라이언트
    private GoogleApiClient mGoogleApiClient; //구글인증에필요
    // 파이어베이스 인증 객체 생성
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    // 구글  로그인 버튼
    private SignInButton googleButton;
    //사용자
    private FirebaseUser mCurrentUser;
    //페이스북로그인
    private CallbackManager callbackManager;
    LoginButton facebookLoginButton;
    //로딩다이얼로그
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(getApplicationContext()); //페북관련(setConetentView 전에해야함  SDK의 앱 활성화 지원 도구가 호출)
        //AppEventsLogger.activateApp(this); //페북관련

        // 임시저장값 불러오기
        mAppData = getSharedPreferences("appData", MODE_PRIVATE);
        load();


        mIdEditText = findViewById(R.id.login_pt_id);
        mPasswordEditText = findViewById(R.id.login_pt_password);
        mCheckBox = findViewById(R.id.checkBox);
        googleButton = findViewById(R.id.login_btn_google);
        facebookLoginButton = findViewById(R.id.login_btn_facebook);
        mAuth = FirebaseAuth.getInstance(); //firebaseAuth 객체의 공유 인스턴스를 가져옵니다.
        //페북로그인 관련( CallbackManager.Factory.create를 호출하여 로그인 응답을 처리할 콜백 관리자를 만듭니다.)
        callbackManager = CallbackManager.Factory.create();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = firebaseAuth.getCurrentUser();
                if(mCurrentUser != null){
                    //이 유저 로그인시
                 /*   Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);*/
                }else{
                    //해당 로그아웃시
                }
            }
        };


        // 이전에 로그인 정보를 저장시킨 기록이 있다면 불러옴 (로그인정보저장 불러오기)
        if (isSavedLogData) {
            mIdEditText.setText(mEmail);
            mPasswordEditText.setText(mPassword);
            mCheckBox.setChecked(isSavedLogData);
        }


        //페이스북로그인
        facebookLoginButton.setReadPermissions("email");
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });

        //로그인 버튼 클릭
        loginButton();

        //회원가입 버튼 클릭
        signupButton();

        //구글로그인관련 소스
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this) //기본으로 세팅해줌
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        findViewById(R.id.login_btn_google).setOnClickListener(this);


    }

    private void loginButton(){
        findViewById(R.id.login_btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmail = mIdEditText.getText().toString().trim();
                mPassword = mPasswordEditText.getText().toString().trim();
                if (mEmail.equals("") || mPassword.equals("")) {
                    Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                } else {
                    loading(); //로딩다이얼로그
                    mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                                        save(); //로그인 정보저장
                                        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                        startActivity(intent);
                                        loadingEnd();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                                        loadingEnd();
                                        mIdEditText.startAnimation(shake);
                                        mPasswordEditText.startAnimation(shake);

                                    }
                                }
                            });
                }
            }
        });
    }

    private void signupButton(){
        findViewById(R.id.login_btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override //When initializing your Activity, check to see if the user is currently signed in.
    protected void onStart() {
        super.onStart();
        mCurrentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    // 설정값을 저장하는 함수
    private void save() {
        // SharedPreferences 객체만으론 저장 불가능 Editor 사용
        SharedPreferences.Editor editor = mAppData.edit();

        // 에디터객체.put타입( 저장시킬 이름, 저장시킬 값 )
        // 저장시킬 이름이 이미 존재하면 덮어씌움
        editor.putBoolean("SAVE_LOGIN_DATA", mCheckBox.isChecked());
        editor.putString("ID", mIdEditText.getText().toString().trim());
        editor.putString("PWD", mPasswordEditText.getText().toString().trim());
        // apply, commit 을 안하면 변경된 내용이 저장되지 않음
        editor.apply();
    }

    // 설정값을 불러오는 함수
    private void load() {
        // SharedPreferences 객체.get타입( 저장된 이름, 기본값 )
        // 저장된 이름이 존재하지 않을 시 기본값
        isSavedLogData = mAppData.getBoolean("SAVE_LOGIN_DATA", false);
        mEmail = mAppData.getString("ID", "");
        mPassword = mAppData.getString("PWD", "");
    }

    //구글인증연결 실패시
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //구글로그인버튼 눌렀을 때 처리
    @Override
    public void onClick(View v) {
        Intent signInintent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInintent, CODE_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 페이스북 콜백 등록
        callbackManager.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CODE_SIGN_IN) { //구글로그인버튼 누르고 응답결과
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) { //로그인 성공시
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(this, "구글 로그인을 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) { //task에서 다양한 정보를 담고있기 때문에 잘 사용하면된다.
                        if (!task.isSuccessful()) { //실패했다면
                            Toast.makeText(LoginActivity.this, "인증 실패하였습니다. 이미 가입된 이메일일수도 있습니다.", Toast.LENGTH_LONG).show();

                        } else { //성공했으면 다시 로그인액티비티에서 프로필액티비티로 가게해주면된다.
                            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        }
                    }
                });
    }

    // 페이스북 로그인 이벤트
    // 사용자가 정상적으로 로그인한 후 페이스북 로그인 버튼의 onSuccess 콜백 메소드에서 로그인한 사용자의
    // 액세스 토큰을 가져와서 Firebase 사용자 인증 정보로 교환하고,
    // Firebase 사용자 인증 정보를 사용해 Firebase에 인증.
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            mCurrentUser = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, " 페이스북 계정에 사용된 이메일로 이미 가입하셨습니다. 이메일과 비밀번호로 다시 시도해주세요.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void loading() {
        //로딩
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setMessage("로그인 중입니다~^^");
                        progressDialog.show();
                    }
                }, 0);
    }

    public void loadingEnd() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 0);
    }
}
