package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendImageFileButton, SendMessageButton;
    private EditText userMessageInput;
    private RecyclerView userMessagesList;
    private TextView receiverName;
    private CircleImageView receiverProfileImage;

    private final List<Messages> messagesList = new ArrayList<>();
    private MessagesAdapter messagesAdapter;

    private String messageReceiverID;
    private String messageReceiverName;
    private String messageSenderID;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();
        DisplayReceiverInfo();
        receiverProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getApplicationContext(), PersonProfileActivity.class);
                profileIntent.putExtra("visit_user_id", messageReceiverID);
                startActivity(profileIntent);
            }
        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();
    }

    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    Messages messages = snapshot.getValue(Messages.class);
                    messagesList.add(messages);
                    messagesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void DisplayReceiverInfo() {
        receiverName.setText(messageReceiverName);
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    final String profileImage = snapshot.child("profileimage").getValue().toString();
                    PicassoStuff(getApplicationContext(), profileImage, receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void SendMessage() {
        String messageText = userMessageInput.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Please Type Something..", Toast.LENGTH_SHORT).show();
        }else{
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();
            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("E, dd MMM", Locale.US);
            String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a", Locale.US);
            String saveCurrentTime = currentTime.format(calForDate.getTime());

            Map<String, Object> messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);

            Map<String, Object> messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }else{
                        Toast.makeText(ChatActivity.this, "Error Occured!", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                }
            });
        }
    }
    private static void PicassoStuff(Context context, String loadImage, ImageView intoImage) {
        Picasso.Builder builder = new Picasso.Builder(context).indicatorsEnabled(true);
        builder.downloader(new OkHttp3Downloader(context,Integer.MAX_VALUE));
        builder.listener(new Picasso.Listener()
        {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
            {
                String message = exception.getMessage();
                Log.i("Error", message);
            }
        });
        builder.build().load(loadImage).placeholder(R.drawable.profile).fit().centerCrop().into(intoImage);
    }
    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        getSupportActionBar().setCustomView(action_bar_view);


        SendImageFileButton = findViewById(R.id.send_image_file_button);
        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_message);
        userMessagesList = findViewById(R.id.messages_list_users);
        receiverName = findViewById(R.id.custom_profile_name);
        receiverProfileImage = findViewById(R.id.custom_profile_image);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        messagesAdapter = new MessagesAdapter(messagesList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);
    }
}