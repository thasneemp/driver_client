package com.launcher.mummu.cabclient.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.launcher.mummu.cabclient.R;
import com.launcher.mummu.cabclient.storage.CabStorageUtil;
import com.launcher.mummu.cabclient.storage.FirebaseStorage;

import java.util.Arrays;

/**
 * Created by muhammed on 2/20/2017.
 */

public class LoginActivity extends Container implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, FacebookCallback<LoginResult> {
    private static final int RC_SIGN_IN = 10214;
    private static final String PERMISSION_PROFILE = "public_profile";
    private static final String PERMISSION_EMAIL = "email";

    FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d("TEST", "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Log.d("TEST", "onAuthStateChanged:signed_out");
            }
            // ...
        }
    };
    private GoogleApiClient mGoogleApiClient;
    private Button mGoogleSignInButton;
    private Button mFacebookSignInButton;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUI();
    }

    private void setUI() {
        mGoogleSignInButton = (Button) findViewById(R.id.sign_in_button);
        mFacebookSignInButton = (Button) findViewById(R.id.facebook);
        mGoogleSignInButton.setOnClickListener(this);
        mFacebookSignInButton.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, this);


    }

    private ProgressDialog showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else {
            progressDialog.show();
        }
        return progressDialog;
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthListener);

        if (mGoogleApiClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "Please wait...!", Toast.LENGTH_SHORT).show();
        switch (v.getId()) {
            case R.id.sign_in_button:
                signInGoogle();
                break;
            case R.id.facebook:
                signInFacebook();
                break;
        }

    }

    private void signInFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList(PERMISSION_PROFILE, PERMISSION_EMAIL));
    }

    private void signInGoogle() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            showProgressDialog();
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        } else {
            Toast.makeText(LoginActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        checkAuthentication(task);
                    }
                });
    }


    @Override
    public void onSuccess(LoginResult loginResult) {
        showProgressDialog();
        handleFacebookAccessTokenWithFireBase(loginResult.getAccessToken());
    }

    private void handleFacebookAccessTokenWithFireBase(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                checkAuthentication(task);
            }
        });
    }

    @Override
    public void onCancel() {
        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(FacebookException error) {
        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
    }

    private void checkAuthentication(Task<AuthResult> task) {
        showProgressDialog();
        if (!task.isSuccessful()) {

            Toast.makeText(LoginActivity.this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LoginActivity.this, "Authentication Success",
                    Toast.LENGTH_SHORT).show();
            CabStorageUtil.setUUid(LoginActivity.this, CabStorageUtil.USER_UUID, task.getResult().getUser().getUid());
            CabStorageUtil.setLogging(LoginActivity.this, CabStorageUtil.IS_LOGGED, true);
            CabStorageUtil.setUsername(LoginActivity.this, CabStorageUtil.USER_NAME, task.getResult().getUser().getEmail());
            CabStorageUtil.setUserDisplayName(LoginActivity.this, task.getResult().getUser().getDisplayName());
            CabStorageUtil.setUserProfile(LoginActivity.this, task.getResult().getUser().getPhotoUrl());

            //Inserting to firebase
            FirebaseStorage.insertUserInfo(task.getResult().getUser());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}
