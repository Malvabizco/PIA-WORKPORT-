package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton buttonConfig;
    private MaterialCardView cardAccessControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        buttonConfig = findViewById(R.id.buttonConfig);
        cardAccessControl = findViewById(R.id.cardAccessControl);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Configurar el botón de engranaje para abrir el sidebar
        buttonConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Configurar el click en Control de Acceso
        cardAccessControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, AccessActivity.class));
            }
        });

        // Configurar botones dentro del sidebar
        setupSidebar(user);
    }

    private void setupSidebar(FirebaseUser user) {
        Button btnNightMode = findViewById(R.id.nav_night_mode);
        Button btnAccount = findViewById(R.id.nav_account);
        Button btnRegisterAdmin = findViewById(R.id.nav_register_admin);
        Button btnLogout = findViewById(R.id.nav_logout);

        if (user != null && user.getEmail() != null && user.getEmail().contains("admin")) {
            btnRegisterAdmin.setVisibility(View.VISIBLE);
        } else {
            btnRegisterAdmin.setVisibility(View.GONE);
        }

        btnNightMode.setOnClickListener(v -> {
            Toast.makeText(this, "Modo Nocturno (Próximamente)", Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        btnAccount.setOnClickListener(v -> {
            String email = (user != null) ? user.getEmail() : "No identificado";
            Toast.makeText(this, "Cuenta: " + email, Toast.LENGTH_SHORT).show();
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        btnRegisterAdmin.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, RegisterActivity.class));
            drawerLayout.closeDrawer(GravityCompat.END);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });
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
