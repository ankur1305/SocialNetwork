package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userProfName, userStatus, userPhone, userGender, userRelation, userDOB;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = (Toolbar) findViewById(R.id.settings_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        userName = findViewById(R.id.settings_username);
        userProfImage = findViewById(R.id.settings_profile_image);
        userStatus = findViewById(R.id.settings_status);
        userPhone = findViewById(R.id.settings_phone);
        userProfName = findViewById(R.id.settings_profile_full_name);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDOB = findViewById(R.id.settings_dob);
        updateAccountSettingsButton = findViewById(R.id.update_account_settings_button);


        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String myUserName = snapshot.child("username").getValue().toString();
                    String myProfileImage = snapshot.child("profileimage").getValue().toString();
                    String myProfileStatus = snapshot.child("status").getValue().toString();
                    String myProfilePhone = snapshot.child("phone").getValue().toString();
                    String myUserProfileName = snapshot.child("fullname").getValue().toString();
                    String myProfileGender = snapshot.child("gender").getValue().toString();
                    String myProfileRelation = snapshot.child("relationshipstatus").getValue().toString();
                    String myProfileDOB = snapshot.child("dob").getValue().toString();

                    userName.setText(myUserName);
                    PicassoStuff(getApplicationContext(), myProfileImage, userProfImage);
                    userStatus.setText(myProfileStatus);
                    userPhone.setText(myProfilePhone);
                    userProfName.setText(myUserProfileName);
                    userGender.setText(myProfileGender);
                    userRelation.setText(myProfileRelation);
                    userDOB.setText(myProfileDOB);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static void PicassoStuff(Context context, String loadImage, ImageView intoImage){
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.listener(new Picasso.Listener()
        {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
            {
                String message = exception.getMessage();
                Log.i("Error", message);
            }
        });
        builder.build().load(loadImage).fit().into(intoImage);
    }
}