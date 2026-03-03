package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private MaterialButtonToggleGroup roleToggleButtonGroup;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // PARCHE DE SOLAPAMIENTO: Aplicar a la vista raíz del layout
        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        roleToggleButtonGroup = findViewById(R.id.roleToggleButtonGroup);
        progressBar = findViewById(R.id.progressBar);

        buttonLogin.setOnClickListener(this);
        textViewRegister.setOnClickListener(this);

        // PERSISTENCIA DE SESIÓN: Validar rol y redirigir
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            checkUserRoleAndRedirect(currentUser.getUid(), null);
        }
    }

    private void checkUserRoleAndRedirect(String uid, String selectedRole) {
        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        db.collection("usuarios").document(uid).get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);

            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();
                String roleInDb = doc.exists() ? doc.getString("role") : "USER";
                String status = doc.exists() ? doc.getString("status") : "active";

                if (!"active".equals(status)) {
                    startActivity(new Intent(MainActivity.this, PendingActivationActivity.class));
                    finish();
                    return;
                }

                // Prioridad al rol seleccionado en login, si no, al de la DB
                String finalRole = (selectedRole != null) ? selectedRole : roleInDb;

                Intent intent;
                if ("ADMIN".equals(finalRole)) {
                    intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                } else if ("GUARD".equals(finalRole)) {
                    intent = new Intent(MainActivity.this, GuardDashboardActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, ProfileActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                // Fallback para errores de red o perfiles manuales
                if (selectedRole != null) {
                    redirectToDashboardByRoleName(selectedRole);
                }
            }
        });
    }

    private void redirectToDashboardByRoleName(String role) {
        Intent intent;
        if ("ADMIN".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("GUARD".equals(role)) {
            intent = new Intent(this, GuardDashboardActivity.class);
        } else {
            intent = new Intent(this, ProfileActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonLogin) {
            loginUser();
        } else if (v.getId() == R.id.textViewRegister) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = roleToggleButtonGroup.getCheckedButtonId();
        final String selectedRole;
        if (checkedId == R.id.btnRoleAdmin) selectedRole = "ADMIN";
        else if (checkedId == R.id.btnRoleGuard) selectedRole = "GUARD";
        else selectedRole = "USER";

        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        checkUserRoleAndRedirect(user.getUid(), selectedRole);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        buttonLogin.setEnabled(true);
                        startActivity(new Intent(this, VerifyEmailActivity.class));
                    }
                }
            } else {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);
                Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_LONG).show();
            }
        });
    }
}
