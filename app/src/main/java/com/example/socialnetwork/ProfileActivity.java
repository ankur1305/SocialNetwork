package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userPhone, userGender, userRelation, userDOB;
    private CircleImageView userProfImage;
    private Button myPostsButton, myFriendsButton;

    private String currentUserID;
    private int friendsCount, postsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.my_username);
        userProfImage = findViewById(R.id.my_profile_pic);
        userStatus = findViewById(R.id.my_profile_status);
        userPhone = findViewById(R.id.my_phone);
        userProfName = findViewById(R.id.my_full_name);
        userGender = findViewById(R.id.my_gender);
        userRelation = findViewById(R.id.my_relationship_status);
        userDOB = findViewById(R.id.my_dob);
        myPostsButton = findViewById(R.id.my_post_button);
        myFriendsButton = findViewById(R.id.my_friends_button);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        DatabaseReference profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        DatabaseReference friendsUserRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        friendsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    friendsCount = (int) snapshot.getChildrenCount();
                    myFriendsButton.setText(Integer.toString(friendsCount) + " Friends");
                }else{
                    myFriendsButton.setText("0 Friends");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        profileUserRef.addValueEventListener(new ValueEventListener() {
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
        postsRef.orderByChild("uid").startAt(currentUserID).endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            postsCount = (int) snapshot.getChildrenCount();
                            myPostsButton.setText(Integer.toString(postsCount) + " Posts");
                        }else{
                            myPostsButton.setText("0 Posts");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        myFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), FriendsActivity.class));
            }
        });

        myPostsButton.setOnClickListener(new View.OnClickListener() {
            @java.lang.Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MyPostsActivity.class));
            }
        });
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