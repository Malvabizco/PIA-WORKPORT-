package com.example.pia_claseordinaria;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class GuardDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardScanQR;
    private ImageButton buttonLogout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_dashboard);

        // PARCHE DE SOLAPAMIENTO
        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        cardScanQR = findViewById(R.id.cardScanQR);
        buttonLogout = findViewById(R.id.buttonLogout);

        cardScanQR.setOnClickListener(v -> startQRScanner());

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escaneando código de acceso...");
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            validateScannedQR(result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void validateScannedQR(String contents) {
        // Formato esperado: UID:userId|KEY:accessKey
        try {
            String[] parts = contents.split("\\|");
            String userId = parts[0].replace("UID:", "");
            String key = parts[1].replace("KEY:", "");

            db.collection("registro_de_entrada")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("key", key)
                    .whereEqualTo("status", "GENERADO")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Cambiamos el estado a AUTORIZADO en Firebase
                            String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            db.collection("registro_de_entrada").document(docId)
                                    .update("status", "AUTORIZADO")
                                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "¡ACCESO AUTORIZADO!", Toast.LENGTH_LONG).show());
                        } else {
                            Toast.makeText(this, "Código no válido o ya utilizado", Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Formato de código no reconocido", Toast.LENGTH_SHORT).show();
        }
    }
}
