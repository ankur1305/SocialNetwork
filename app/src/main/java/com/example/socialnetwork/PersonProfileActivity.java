package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    private DatabaseReference FriendRequestRef, UsersRef;
    private FirebaseAuth mAuth;

    private String senderUserID, receiverUserID, CURRENT_STATE;


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

                    MaintainanceOfButtons();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if(!senderUserID.equals(receiverUserID)){
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendRequestButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestToAPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }
                }
            });
        }else{
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }


    }



    private void MaintainanceOfButtons() {
        FriendRequestRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserID)){
                    String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if(request_type.equals("sent")){
                        CURRENT_STATE = "request_sent";
                        SendFriendRequestButton.setText("Cancel Friend Request");
                        SendFriendRequestButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                        DeclineFriendRequestButton.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendFriendRequestToAPerson() {
        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendFriendRequestButton.setText("Cancel Friend Request");
                                                SendFriendRequestButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send Friend Request");
                                                SendFriendRequestButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
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
        CURRENT_STATE = "not_friends";

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
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