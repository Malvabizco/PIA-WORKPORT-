package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardPendingUsers, cardRegisterUser, cardPostAnnouncement, cardViewComplaints, cardSecurityChat;
    private ImageButton buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cardPendingUsers = findViewById(R.id.cardPendingUsers);
        cardRegisterUser = findViewById(R.id.cardRegisterUser);
        cardPostAnnouncement = findViewById(R.id.cardPostAnnouncement);
        cardViewComplaints = findViewById(R.id.cardViewComplaints);
        cardSecurityChat = findViewById(R.id.cardSecurityChat);
        buttonLogout = findViewById(R.id.buttonLogout);

        cardPendingUsers.setOnClickListener(v -> startActivity(new Intent(this, PendingUsersActivity.class)));
        cardRegisterUser.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        cardPostAnnouncement.setOnClickListener(v -> startActivity(new Intent(this, AdminPostAnnouncementActivity.class)));
        cardViewComplaints.setOnClickListener(v -> startActivity(new Intent(this, AdminComplaintsActivity.class)));
        cardSecurityChat.setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}