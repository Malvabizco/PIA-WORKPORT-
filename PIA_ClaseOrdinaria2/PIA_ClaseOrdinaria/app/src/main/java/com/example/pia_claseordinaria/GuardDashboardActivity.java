package com.example.pia_claseordinaria;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GuardDashboardActivity extends AppCompatActivity {

    private MaterialCardView cardScanQR;
    private ImageButton buttonLogout;
    private MaterialButton buttonFilterDate;
    private TextView textViewEmpty;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewScanned;
    private ScannedAccessAdapter adapter;
    private List<Map<String, String>> scannedList;
    private static final String CHANNEL_ID = "access_notification_channel";
    private String selectedFilterDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guard_dashboard);

        createNotificationChannel();

        View mainView = findViewById(R.id.guard_root_layout);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        cardScanQR = findViewById(R.id.cardScanQR);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonFilterDate = findViewById(R.id.buttonFilterDate);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        recyclerViewScanned = findViewById(R.id.recyclerViewScanned);

        scannedList = new ArrayList<>();
        adapter = new ScannedAccessAdapter(scannedList);
        recyclerViewScanned.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewScanned.setAdapter(adapter);

        // Por defecto, cargar historial de hoy
        selectedFilterDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        loadHistoryByDate(selectedFilterDate);

        cardScanQR.setOnClickListener(v -> startQRScanner());
        buttonFilterDate.setOnClickListener(v -> showDatePicker());

        buttonLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            selectedFilterDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selected.getTime());
            buttonFilterDate.setText(selectedFilterDate);
            loadHistoryByDate(selectedFilterDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void loadHistoryByDate(String date) {
        db.collection("registro_de_entrada")
                .whereEqualTo("date", date)
                .whereEqualTo("status", "AUTORIZADO")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scannedList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            fetchAndAddToList(doc);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Si falla por falta de índice compuesto, intentamos sin ordenación de Firestore
                    loadHistoryWithoutOrder(date);
                });
    }

    private void loadHistoryWithoutOrder(String date) {
        db.collection("registro_de_entrada")
                .whereEqualTo("date", date)
                .whereEqualTo("status", "AUTORIZADO")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scannedList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        textViewEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textViewEmpty.setVisibility(View.GONE);
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            fetchAndAddToList(doc);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void fetchAndAddToList(DocumentSnapshot accessDoc) {
        String userId = accessDoc.getString("userId");
        String time = accessDoc.getString("time");
        String date = accessDoc.getString("date");

        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    Map<String, String> scanData = new HashMap<>();
                    scanData.put("name", userDoc.getString("fullName") != null ? userDoc.getString("fullName") : "N/A");
                    scanData.put("email", userDoc.getString("email") != null ? userDoc.getString("email") : "N/A");
                    scanData.put("address", userDoc.getString("address") != null ? userDoc.getString("address") : "N/A");
                    scanData.put("phone", userDoc.getString("phone") != null ? userDoc.getString("phone") : "N/A");
                    scanData.put("time", date + " " + time);

                    scannedList.add(scanData);
                    // Ordenar localmente por si acaso
                    adapter.notifyDataSetChanged();
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Acceso";
            String description = "Canal para avisos de entrada";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
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
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String docId = doc.getId();
                            
                            db.collection("registro_de_entrada").document(docId)
                                    .update("status", "AUTORIZADO")
                                    .addOnSuccessListener(aVoid -> {
                                        showNotification("Acceso Autorizado", "Un nuevo usuario ha ingresado al recinto.");
                                        // Recargar la lista si el acceso es de hoy
                                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                        if (selectedFilterDate.equals(today)) {
                                            loadHistoryByDate(today);
                                        } else {
                                            Toast.makeText(this, "¡Acceso Autorizado! (Vea el historial de hoy)", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Código no válido o ya utilizado", Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Formato de código no reconocido", Toast.LENGTH_SHORT).show();
        }
    }
}