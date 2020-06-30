package com.example.socialnetwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;

    private String post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        post_key = getIntent().getExtras().get("PostKey").toString();

        PostCommentButton = findViewById(R.id.post_comment_button);
        CommentInputText = findViewById(R.id.comment_input);
        CommentsList = findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);


    }
}