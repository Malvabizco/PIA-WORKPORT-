package com.example.pia_claseordinaria;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ReservationsActivity extends AppCompatActivity {

    private Spinner spinnerEdificio, spinnerPiso, spinnerHabitacion;
    private Button buttonReservar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        spinnerEdificio = findViewById(R.id.spinnerEdificio);
        spinnerPiso = findViewById(R.id.spinnerPiso);
        spinnerHabitacion = findViewById(R.id.spinnerHabitacion);
        buttonReservar = findViewById(R.id.buttonReservar);

        // Configurar opciones para Edificio (1, 2, 3)
        String[] edificios = {"1", "2", "3"};
        setupSpinner(spinnerEdificio, edificios);

        // Configurar opciones para Piso (1, 2, 3, 4)
        String[] pisos = {"1", "2", "3", "4"};
        setupSpinner(spinnerPiso, pisos);

        // Configurar opciones para Habitación (1, 2, 3, 4, 5)
        String[] habitaciones = {"1", "2", "3", "4", "5"};
        setupSpinner(spinnerHabitacion, habitaciones);

        buttonReservar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edificio = spinnerEdificio.getSelectedItem().toString();
                String piso = spinnerPiso.getSelectedItem().toString();
                String habitacion = spinnerHabitacion.getSelectedItem().toString();

                Toast.makeText(ReservationsActivity.this, 
                    "Reservación: Edificio " + edificio + ", Piso " + piso + ", Habitación " + habitacion, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupSpinner(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, options);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
