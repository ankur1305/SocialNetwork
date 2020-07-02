package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView MyFriendsList;
    private DatabaseReference FriendsRef, UsersRef;
    private String online_user_id;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        MyFriendsList = findViewById(R.id.friends_list);
        MyFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        MyFriendsList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);

        DisplayAllFriends();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateUserStatus("Online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        UpdateUserStatus("Offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("Offline");
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(FriendsRef, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {

                holder.setDate(model.getDate());
                final String userIDs = getRef(position).getKey();

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            final String userName = snapshot.child("fullname").getValue().toString();
                            final String profileImage = snapshot.child("profileimage").getValue().toString();

                            holder.setFullName(userName);
                            holder.setProfileimage(getApplication(), profileImage);
                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]{
                                      userName + "'s Profile",
                                      "Send Message"
                                    };
                                    AlertDialogStuff(options, userIDs, userName);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        MyFriendsList.setAdapter(firebaseRecyclerAdapter);
    }

    private void AlertDialogStuff(CharSequence options[], final String userIDs, final String userName) {
        android.app.AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FriendsActivity.this, R.style.AlertDialogTheme);
        alertDialogBuilder
                .setTitle("Select option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Intent profileIntent = new Intent(getApplicationContext(), PersonProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", userIDs);
                            startActivity(profileIntent);
                        }
                        if(which == 1){
                            Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                            chatIntent.putExtra("visit_user_id", userIDs);
                            chatIntent.putExtra("userName", userName);
                            startActivity(chatIntent);
                        }
                    }
                })
                .show();
        alertDialogBuilder.create();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String fullname){
            TextView fullName = (TextView) mView.findViewById(R.id.all_users_profile_name);
            fullName.setText(fullname);
        }
        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            PicassoStuff(ctx,profileimage,image);
        }

        public void setDate(String date) {
            TextView friendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends Since " + date);
        }
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

    public void UpdateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("E, dd MMM");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("h:mm a");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        Map<String, Object> currentStatusMap = new HashMap<>();
        currentStatusMap.put("time", saveCurrentTime);
        currentStatusMap.put("date", saveCurrentDate);
        currentStatusMap.put("type", state);

        UsersRef.child(online_user_id).child("userState").updateChildren(currentStatusMap);
    }
}