package com.example.productiv.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.productiv.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerifyActivity extends AppCompatActivity {

    TextView tvEmail;
    Button btnResendEmail;
    Button btnContact;
    public static final String TAG = "EmailVerifyActivity";

    // Initialize Firebase Auth
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseUser user = mAuth.getCurrentUser();

    boolean isSent;
    Handler handler = new Handler();
    Runnable runnable;
    int verifyDelay = 1000; // 1 second
    int resendDelay = 15000; // 15 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verify);

        tvEmail = findViewById(R.id.tvEmail);
        btnResendEmail = findViewById(R.id.btnResendEmail);
        btnContact = findViewById(R.id.btnContact);

        tvEmail.setText(user.getEmail());

        sendEmail();

        btnResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnResendEmail.setEnabled(false);
                sendEmail();
                // Delay user from clicking again
                new Handler().postDelayed(new Runnable() {
                   @Override
                    public void run() {
                       btnResendEmail.setEnabled(true);
                   }
                }, resendDelay);
            }
        });
    }

    public void sendEmail() {
        Log.i(TAG, "sendEmail called");
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmailVerifyActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            isSent = true;
                        } else {
                            Log.i(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EmailVerifyActivity.this,
                                    "Failed to send verification email. Try again later.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, verifyDelay);
                // Run this code every second
                user.reload();
                if (user.isEmailVerified()) {
                    Log.i(TAG, "Email is verified!");
                    goMainActivity();
                }
            }
        }, verifyDelay);
        super.onResume();
    }

    // Stop handler when activity not visible
    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    public void goMainActivity() {
        Log.i(TAG, "Went to MainActivity from EmailVerifyActivity");
        Intent i = new Intent(EmailVerifyActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}