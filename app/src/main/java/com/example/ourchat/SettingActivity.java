package com.example.ourchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button UpdateAccountSetting;
    private EditText UserName, UserStatus;
    private CircleImageView UserProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPic = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializerFields();

        UserName.setVisibility(View.INVISIBLE);


        UpdateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSetting();
            }
        });


        RetrieveUserInfo();

        UserProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPic);
            }
        });
    }




    private void InitializerFields()
    {
        UpdateAccountSetting = (Button) findViewById(R.id.update_setting_button);
        UserName = (EditText) findViewById(R.id.set_user_name);
        UserStatus = (EditText) findViewById(R.id.set_profile_status);
        UserProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPic  && resultCode==RESULT_OK  &&  data!=null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

        }
    }

    private void UpdateSetting() {
        String setUserName = UserName.getText().toString();
        String setStatus = UserStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please Write your User Name", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please Write your Status", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, String> profileMap = new HashMap<>();
              profileMap.put("uid", currentUserID);
              profileMap.put("name", setUserName);
              profileMap.put("status", setStatus);

            RootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               SendUserToMainActivity();
                               Toast.makeText(SettingActivity.this, "Profile Updated Successfull", Toast.LENGTH_SHORT).show();
                           }
                           else {
                               String message = task.getException().toString();
                               Toast.makeText(SettingActivity.this, "Error" +message, Toast.LENGTH_SHORT).show();
                           }
                        }
                    });
        }


    }

    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image"))))
                        {
                            String retriveUserName = snapshot.child("name").getValue().toString();
                            String retriveStatus = snapshot.child("status").getValue().toString();
                            String retriveProfileImage = snapshot.child("image").getValue().toString();

                            UserName.setText(retriveUserName);
                            UserStatus.setText(retriveStatus);
                        }
                        else if ((snapshot.exists()) && (snapshot.hasChild("name")))
                        {
                            String retriveUserName = snapshot.child("name").getValue().toString();
                            String retriveStatus = snapshot.child("status").getValue().toString();

                            UserName.setText(retriveUserName);
                            UserStatus.setText(retriveStatus);
                        }
                        else
                        {
                            UserName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingActivity.this, "Please Setup Your Profile Information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}