package com.example.pia_claseordinaria;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AccessControlActivity extends AppCompatActivity {

    private ImageView imageViewQR;
    private TextView textViewQRStatus;
    private Button buttonGenerateQR;
    private ImageButton buttonBack;
    private RecyclerView recyclerViewHistory;
    private AccessAdapter adapter;
    private List<AccessRecord> accessHistoryList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_control);

        // SOLUCIÓN AL SOLAPAMIENTO
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imageViewQR = findViewById(R.id.imageViewQR);
        textViewQRStatus = findViewById(R.id.textViewQRStatus);
        buttonGenerateQR = findViewById(R.id.buttonGenerateQR);
        buttonBack = findViewById(R.id.buttonBack);
        recyclerViewHistory = findViewById(R.id.recyclerViewAccessHistory);

        accessHistoryList = new ArrayList<>();
        adapter = new AccessAdapter(accessHistoryList);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());
        buttonGenerateQR.setOnClickListener(v -> generateNewAccessQR());

        loadAccessHistory();
    }

    private void loadAccessHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        // Ordenamos por el nuevo campo timestamp para tener el más reciente SIEMPRE arriba
        db.collection("registro_de_entrada")
                .whereEqualTo("userId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Si falla (probablemente falta índice), intentamos sin orden para no bloquear la app
                        loadHistoryWithoutOrder(user.getUid());
                        return;
                    }
                    if (value != null) {
                        updateUI(value.getDocuments());
                    }
                });
    }

    private void loadHistoryWithoutOrder(String uid) {
        db.collection("registro_de_entrada")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((value, error) -> {
                    if (value != null) updateUI(value.getDocuments());
                });
    }

    private void updateUI(List<DocumentSnapshot> docs) {
        accessHistoryList.clear();
        for (DocumentSnapshot doc : docs) {
            AccessRecord record = doc.toObject(AccessRecord.class);
            if (record != null) accessHistoryList.add(record);
        }
        
        // Si cargamos sin orden de Firestore, ordenamos localmente por timestamp
        accessHistoryList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        
        adapter.notifyDataSetChanged();
        if (!accessHistoryList.isEmpty()) {
            displayLatestQR(accessHistoryList.get(0));
        }
    }

    private void generateNewAccessQR() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String key = generateRandomKey(8);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        AccessRecord record = new AccessRecord(user.getUid(), user.getEmail(), key, date, time);
        record.status = "GENERADO"; // Aseguramos el estado correcto

        db.collection("registro_de_entrada").add(record)
                .addOnSuccessListener(doc -> Toast.makeText(this, "¡Nuevo QR generado!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Fallo al generar: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void displayLatestQR(AccessRecord record) {
        String qrContent = "UID:" + record.userId + "|KEY:" + record.key;
        try {
            Bitmap bitmap = encodeAsBitmap(qrContent);
            imageViewQR.setImageBitmap(bitmap);
            textViewQRStatus.setText("Estado: " + record.status);
            textViewQRStatus.setTextColor(record.status.equals("AUTORIZADO") ? Color.parseColor("#2E7D32") : Color.GRAY);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private String generateRandomKey(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 512, 512);
        int width = bitMatrix.getWidth(), height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; width > x; x++) {
            for (int y = 0; height > y; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
