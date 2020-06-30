package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userProfName, userStatus, userPhone, userGender, userRelation, userDOB;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;
    private ProgressDialog loadingBar;

    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;


    private String currentUserID, downloadImageUrl;
    final static int Gallery_Pick = 1;

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
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        userName = findViewById(R.id.settings_username);
        userProfImage = findViewById(R.id.settings_profile_image);
        userStatus = findViewById(R.id.settings_status);
        userPhone = findViewById(R.id.settings_phone);
        userProfName = findViewById(R.id.settings_profile_full_name);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDOB = findViewById(R.id.settings_dob);
        updateAccountSettingsButton = findViewById(R.id.update_account_settings_button);

        loadingBar = new ProgressDialog(this);


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

        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent, Gallery_Pick);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please Wait, While We Are Updating Your Profile Image");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                userProfImage.setImageURI(resultUri);
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                final UploadTask uploadTask = filePath.putFile(resultUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String message = e.toString();
                        Toast.makeText(SettingsActivity.this, "Some Error Occured"+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SettingsActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }
                                downloadImageUrl = filePath.getDownloadUrl().toString();

                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){

                                    downloadImageUrl = task.getResult().toString();
                                    settingsUserRef.child("profileimage").setValue(downloadImageUrl);
                                    Toast.makeText(SettingsActivity.this, "Saved To Database Successfully", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                });
            }else{
                Toast.makeText(this, "An Error Occured, Try Again", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String status = userStatus.getText().toString();
        String phone = userPhone.getText().toString();
        String profilename = userProfName.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();
        String dob = userDOB.getText().toString();

        UpdateAccountInfo(username, status, phone, profilename, gender, relation, dob);

    }

    private void UpdateAccountInfo(String username, String status, String phone, String profilename, String gender, String relation, String dob) {
        loadingBar.setTitle("Save Information");
        loadingBar.setMessage("Please Wait, While We Are Updating Your Account Details");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("status", status);
        userMap.put("phone", phone);
        userMap.put("fullname", profilename);
        userMap.put("gender", gender);
        userMap.put("relationshipstatus", relation);
        userMap.put("dob", dob);

        settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Details Updated Successfully", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }else{
                    Toast.makeText(SettingsActivity.this, "Some Error Occured, Try Again!", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
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