package com.example.ourchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity
{
    private Button SendVerificationCodeBtn, VerifyBtn;
    private EditText InputPhoneNumber, InputVerificationCode;
    private String mVerficationID;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        SendVerificationCodeBtn = (Button) findViewById(R.id.send_ver_code_btn);
        VerifyBtn = (Button) findViewById(R.id.verify_btn);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        SendVerificationCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
               String phoneNumber = InputPhoneNumber.getText().toString();
               if (TextUtils.isEmpty(phoneNumber)){
                   Toast.makeText(PhoneLoginActivity.this, "Please enter your phone number....", Toast.LENGTH_SHORT).show();
               }else {
                   loadingBar.setTitle("Phone Verification");
                   loadingBar.setMessage("Please wait, while we are authenticating your phone");
                   loadingBar.setCanceledOnTouchOutside(false);
                   loadingBar.show();
                   PhoneAuthProvider.getInstance().verifyPhoneNumber(
                           phoneNumber,
                           60,
                           TimeUnit.SECONDS,
                           PhoneLoginActivity.this,
                           callbacks);
               }
            }
        });

        VerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationCodeBtn.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Write your Verification Code", Toast.LENGTH_SHORT).show();
                }
                else {

                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, while we are verifying Verification code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerficationID, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted( PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please Enter Your correct Phone Number with your Countru code", Toast.LENGTH_SHORT).show();

                SendVerificationCodeBtn.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifyBtn.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent (String VerificationID, PhoneAuthProvider.ForceResendingToken token){
                mVerficationID = VerificationID;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code Has been sent, please check and Verify now.", Toast.LENGTH_SHORT).show();

                SendVerificationCodeBtn.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifyBtn.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential (PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                    public void onComplete(@NonNull Task<AuthResult> task){
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're log in Successfully.", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}