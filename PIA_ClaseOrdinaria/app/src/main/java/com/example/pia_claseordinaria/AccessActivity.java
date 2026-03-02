package com.example.pia_claseordinaria;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AccessActivity extends AppCompatActivity {

    private ImageView imageViewQR;
    private TextView textViewQRInfo;
    private Button buttonGenerateQR;
    private ImageButton buttonBack;
    
    private RecyclerView recyclerViewHistory;
    private AccessHistoryAdapter adapter;
    private List<AccessRecord> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        imageViewQR = findViewById(R.id.imageViewQR);
        textViewQRInfo = findViewById(R.id.textViewQRInfo);
        buttonGenerateQR = findViewById(R.id.buttonGenerateQR);
        buttonBack = findViewById(R.id.buttonBack);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);

        // Configurar RecyclerView
        historyList = new ArrayList<>();
        adapter = new AccessHistoryAdapter(historyList);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        buttonBack.setOnClickListener(v -> finish());

        buttonGenerateQR.setOnClickListener(v -> generateAccessQR());
    }

    private void generateAccessQR() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        String userName = user.getEmail();
        String accessKey = generateRandomKey(8);
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String fullDateTime = currentDate + " - " + currentTime.substring(0, 5);

        String qrData = "Usuario: " + userName + "\n" +
                        "Clave: " + accessKey + "\n" +
                        "Fecha: " + currentDate + "\n" +
                        "Hora: " + currentTime;

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrData, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

            imageViewQR.setImageBitmap(bitmap);
            imageViewQR.setAlpha(1.0f);
            textViewQRInfo.setText("¡Código generado con éxito!\nEste código es personal e intransferible.");
            
            // Añadir al historial (al principio de la lista)
            historyList.add(0, new AccessRecord(fullDateTime, accessKey));
            adapter.notifyItemInserted(0);
            recyclerViewHistory.scrollToPosition(0);

            Toast.makeText(this, "QR Generado", Toast.LENGTH_SHORT).show();

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateRandomKey(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
