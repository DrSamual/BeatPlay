package com.beat.play.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.beat.play.BuildConfig;
import com.beat.play.LoginActivity;
import com.beat.play.R;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setText("সংস্করণ " + BuildConfig.VERSION_NAME);

        view.findViewById(R.id.rowAdmin).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), LoginActivity.class)));

        view.findViewById(R.id.rowShare).setOnClickListener(v -> shareApp());

        view.findViewById(R.id.rowAbout).setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "BeatPlay — লাইভ টিভি ও মুভি স্ট্রিমিং অ্যাপ", Toast.LENGTH_LONG).show());
    }

    private void shareApp() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        send.putExtra(Intent.EXTRA_TEXT,
                "BeatPlay অ্যাপটি দেখুন — লাইভ টিভি ও মুভি স্ট্রিমিং!");
        startActivity(Intent.createChooser(send, "অ্যাপ শেয়ার করুন"));
    }
}
