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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView ClickPostImage;
    private TextView ClickPostDescription;
    private Button EditPostButton, DeletePostButton;

    private DatabaseReference clickPostRef;

    private String PostKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        ClickPostImage = (ImageView) findViewById(R.id.click_post_image);
        ClickPostDescription = (TextView) findViewById(R.id.click_post_description);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String description = snapshot.child("description").getValue().toString();
                String image = snapshot.child("postimage").getValue().toString();

                ClickPostDescription.setText(description);
                PicassoStuff(getApplicationContext(),image,ClickPostImage);

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