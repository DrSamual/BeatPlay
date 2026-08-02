package com.beat.play;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.beat.play.data.DataStore;
import com.beat.play.util.SHA256;
import com.google.firebase.database.DataSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etPassword;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvError);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String input = etPassword.getText().toString().trim();
        tvError.setVisibility(View.GONE);

        if (input.isEmpty()) {
            showError("পাসওয়ার্ড দিন");
            return;
        }

        DataStore.settings().child("adminPasswordHash").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String storedHash = snapshot.getValue(String.class);
                    String inputHash = SHA256.hash(input);
                    if (storedHash != null && storedHash.equals(inputHash)) {
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                        finish();
                    } else {
                        showError("ভুল পাসওয়ার্ড");
                    }
                } else {
                    showError("অ্যাডমিন পাসওয়ার্ড সেট করা নেই।\nREADME-র সেটআপ গাইড দেখুন।");
                }
            } else {
                showError("ইন্টারনেট কানেকশন পরীক্ষা করুন");
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
