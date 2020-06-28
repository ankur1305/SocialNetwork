package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, Phone;
    private CircleImageView profileImage;
    private Button saveInformationButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserId, downloadImageUrl;
    final static int Gallery_Pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        Phone = (EditText) findViewById(R.id.setup_phone);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        saveInformationButton = (Button) findViewById(R.id.setup_information_button);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryintent = new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent, Gallery_Pick);
            }
        });

//        UsersRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    if(dataSnapshot.hasChild("profileimage")){
//                        String image = dataSnapshot.child("profileimage").getValue().toString();
//                        Glide.with(SetupActivity.this).load(image).circleCrop().placeholder(R.drawable.profile).into(profileImage);
//                    }
//                    else{
//                        Toast.makeText(SetupActivity.this, "Please Select Profile Image...", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
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
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();
                profileImage.setImageURI(resultUri);
                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                final UploadTask uploadTask = filePath.putFile(resultUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String message = e.toString();
                        Toast.makeText(SetupActivity.this, "Some Error Occured"+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SetupActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
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
                                    UsersRef.child("profileimage").setValue(downloadImageUrl);
                                    Toast.makeText(SetupActivity.this, "Saved To Database Successfully", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    }
                });



//                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful()){
//                            Toast.makeText(SetupActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
//                            final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//                            UsersRef.child("profileimage").setValue(downloadUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful()){
////                                                Intent selfIntent = new Intent(getApplicationContext(), SetupActivity.class);
////                                                startActivity(selfIntent);
//                                                Toast.makeText(SetupActivity.this, "Profile Image Update Successfully", Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }else{
//                                                String message = task.getException().getMessage();
//                                                Toast.makeText(SetupActivity.this, "Something Error Occured" + message, Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                });
            }else{
                Toast.makeText(this, "An Error Occured, Try Again", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String phone = Phone.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(fullname)){
            Toast.makeText(SetupActivity.this, "Please Fill Your Informations..", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please Wait, While We Are Saving Your Information");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            if(phone == null){
                userMap.put("phone", "null");
            }
            else{
                userMap.put("phone", phone);
            }
            userMap.put("status", "Hey There, I Am Using Social Network.");
            userMap.put("gender", "null");
            userMap.put("dob", "null");
            userMap.put("relationshipstatus", "null");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        loadingBar.dismiss();
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account Has Been Created.", Toast.LENGTH_SHORT).show();
                    }else{
                        loadingBar.dismiss();
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Something Error Occcured " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        finish();
    }
}