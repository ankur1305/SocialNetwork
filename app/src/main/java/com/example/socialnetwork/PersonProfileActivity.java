package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userPhone, userGender, userRelation, userDOB;
    private CircleImageView userProfImage;
    private Button SendFriendRequestButton, DeclineFriendRequestButton;

    private DatabaseReference profileUserRef, UsersRef;
    private FirebaseAuth mAuth;

    private String senderUserID, receiverUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        InitializeFields();

        UsersRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
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

                    userName.setText("@" + myUserName);
                    PicassoStuff(getApplicationContext(), myProfileImage, userProfImage);
                    userStatus.setText(myProfileStatus);
                    userPhone.setText("Phone : " + myProfilePhone);
                    userProfName.setText(myUserProfileName);
                    userGender.setText("Gender : " + myProfileGender);
                    userRelation.setText("Relationship Status : " + myProfileRelation);
                    userDOB.setText("DOB : " + myProfileDOB);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void InitializeFields() {
        userName = findViewById(R.id.person_username);
        userProfImage = findViewById(R.id.person_profile_pic);
        userStatus = findViewById(R.id.person_profile_status);
        userPhone = findViewById(R.id.person_phone);
        userProfName = findViewById(R.id.person_full_name);
        userGender = findViewById(R.id.person_gender);
        userRelation = findViewById(R.id.person_relationship_status);
        userDOB = findViewById(R.id.person_dob);
        SendFriendRequestButton = findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton = findViewById(R.id.person_decline_friend_request_btn);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private static void PicassoStuff(Context context, String loadImage, ImageView intoImage){
        Picasso.Builder builder = new Picasso.Builder(context).indicatorsEnabled(true);
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