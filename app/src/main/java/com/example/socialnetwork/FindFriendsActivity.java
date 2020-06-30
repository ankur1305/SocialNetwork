package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText SearchInputText;
    private ImageButton SearchButton;
    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mToolbar = findViewById(R.id.find_friends_appbar_layout);
        SearchInputText = findViewById(R.id.search_box_input);
        SearchButton = findViewById(R.id.search_people_friends_button);
        SearchResultList = findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchPeopleAndFriends(SearchInputText.getText().toString());
            }
        });

    }

    private void SearchPeopleAndFriends(String searchBoxInput) {

        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();

        Query searchPeopleAndFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchPeopleAndFriendsQuery, FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {
                holder.setFullName(model.getFullname());
                holder.setProfileimage(getApplication(), model.getProfileimage());
                holder.setStatus(model.getStatus());
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        firebaseRecyclerAdapter.startListening();
        SearchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public FindFriendsViewHolder(@NonNull View itemView) {
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
        public void setStatus(String status){
            TextView UserStatus = (TextView) mView.findViewById(R.id.all_users_status);
            UserStatus.setText(status);
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

}