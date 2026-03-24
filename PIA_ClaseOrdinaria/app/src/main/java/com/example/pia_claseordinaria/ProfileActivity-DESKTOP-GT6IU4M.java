package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton buttonConfig;
    private TextView textViewHeader, textViewFooter;
    private View cardReservations, cardAnnouncements, cardComplaints, cardAccessControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // SOLUCIÓN AL SOLAPAMIENTO: Respetar barra de estado
        View mainView = findViewById(R.id.drawer_layout);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        buttonConfig = findViewById(R.id.buttonConfig);
        textViewHeader = findViewById(R.id.textViewHeader);
        textViewFooter = findViewById(R.id.textViewFooter);
        cardReservations = findViewById(R.id.cardReservations);
        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardComplaints = findViewById(R.id.cardComplaints);
        cardAccessControl = findViewById(R.id.cardAccessControl);

        String role = getIntent().getStringExtra("ROLE");

        if ("ADMIN".equals(role)) {
            textViewFooter.setText("Administrador");
            textViewHeader.setText("PANEL DE CONTROL");
        }

        buttonConfig.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        cardReservations.setOnClickListener(v -> startActivity(new Intent(this, ReservationsActivity.class)));
        cardAnnouncements.setOnClickListener(v -> startActivity(new Intent(this, AnnouncementsActivity.class)));
        cardComplaints.setOnClickListener(v -> startActivity(new Intent(this, ComplaintsActivity.class)));
        cardAccessControl.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccessControlActivity.class);
            intent.putExtra("ROLE", role);
            startActivity(intent);
        });

        setupSidebar();
    }

    private void setupSidebar() {
        Button btnLogout = findViewById(R.id.nav_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}