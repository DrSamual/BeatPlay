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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText etPassword;
    private EditText etConfirm;
    private TextInputLayout layoutConfirm;
    private TextView tvError;
    private TextView tvSetupInfo;
    private Button btnLogin;

    private boolean setupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        layoutConfirm = findViewById(R.id.layoutConfirm);
        tvError = findViewById(R.id.tvError);
        tvSetupInfo = findViewById(R.id.tvSetupInfo);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            if (setupMode) {
                attemptSetup();
            } else {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {
        String input = etPassword.getText().toString().trim();
        hideError();

        if (input.isEmpty()) {
            showError("পাসওয়ার্ড দিন");
            return;
        }

        setBusy(true);
        DataStore.settings().child("adminPasswordHash").get().addOnCompleteListener(task -> {
            setBusy(false);
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    String storedHash = snapshot.getValue(String.class);
                    String inputHash = SHA256.hash(input);
                    if (storedHash != null && storedHash.equals(inputHash)) {
                        openAdmin();
                    } else {
                        showError("ভুল পাসওয়ার্ড");
                    }
                } else {
                    enterSetupMode();
                }
            } else {
                showError("ইন্টারনেট কানেকশন পরীক্ষা করুন");
            }
        });
    }

    private void enterSetupMode() {
        setupMode = true;
        layoutConfirm.setVisibility(View.VISIBLE);
        etConfirm.setVisibility(View.VISIBLE);
        tvSetupInfo.setVisibility(View.VISIBLE);
        btnLogin.setText("পাসওয়ার্ড সেট করুন");
        tvSetupInfo.setVisibility(View.VISIBLE);
    }

    private void attemptSetup() {
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();
        hideError();

        if (pass.isEmpty()) {
            showError("পাসওয়ার্ড দিন");
            return;
        }
        if (pass.length() < 4) {
            showError("পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("দুটি পাসওয়ার্ড মিলছে না");
            return;
        }

        setBusy(true);
        DataStore.settings().child("adminPasswordHash").setValue(SHA256.hash(pass))
                .addOnCompleteListener(task -> {
                    setBusy(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "অ্যাডমিন পাসওয়ার্ড সেট হয়েছে", Toast.LENGTH_LONG).show();
                        openAdmin();
                    } else {
                        showError("পাসওয়ার্ড সেট করা যায়নি।\nরুলসে .write: true আছে কিনা দেখুন");
                    }
                });
    }

    private void openAdmin() {
        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
        finish();
    }

    private void setBusy(boolean busy) {
        btnLogin.setEnabled(!busy);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
