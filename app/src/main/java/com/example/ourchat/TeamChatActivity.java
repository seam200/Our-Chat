package com.example.ourchat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class TeamChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SendButtonMessage;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, TeamNameRef, TeamMessageKeyRef;
    private String currentTeamName, currentUserID, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_chat);

        currentTeamName = getIntent().getExtras().get("teamName").toString();
        Toast.makeText(TeamChatActivity.this, currentTeamName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        TeamNameRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(currentTeamName);


        initializerFields();
        
        GetUserInfo();

        SendButtonMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
              SaveMessageInfoToDatabase();
              userMessageInput.setText("");
              mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        TeamNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) 
            {
                if (snapshot.exists()){
                    DisplayMessages(snapshot);
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



    private void initializerFields()
    {
        mToolbar = (Toolbar) findViewById(R.id.team_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentTeamName);

        SendButtonMessage = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_team_message);
        displayTextMessage = (TextView) findViewById(R.id.team_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view_teams);
    }


    private void GetUserInfo()
    {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    private void SaveMessageInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messageKEY = TeamNameRef.push().getKey();

        if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please Write your Team Members Message", Toast.LENGTH_SHORT).show();
        }else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> teamMessageKey = new HashMap<>();
            TeamNameRef.updateChildren(teamMessageKey);
            TeamMessageKeyRef = TeamNameRef.child(messageKEY);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
              messageInfoMap.put("name", currentUserName);
              messageInfoMap.put("message", message);
              messageInfoMap.put("date", currentDate);
              messageInfoMap.put("time", currentTime);

            TeamMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void DisplayMessages(DataSnapshot snapshot)
    {
        Iterator iterator = snapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();


            displayTextMessage.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "        "  + chatDate + "\n\n\n" );
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}