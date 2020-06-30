package com.example.socialnetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userPhone, userGender, userRelation, userDOB;
    private CircleImageView userProfImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.my_user_name);
        userProfImage = findViewById(R.id.my_profile_pic);
        userStatus = findViewById(R.id.my_profile_status);
        userPhone = findViewById(R.id.my_phone);
        userProfName = findViewById(R.id.my_profile_full_name);
        userGender = findViewById(R.id.my_gender);
        userRelation = findViewById(R.id.my_relationship_status);
        userDOB = findViewById(R.id.my_dob);
    }
}