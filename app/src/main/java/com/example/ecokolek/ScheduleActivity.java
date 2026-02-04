package com.example.ecokolek;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity {

    private MaterialButton btnSelectDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_pickup);

        btnSelectDate = findViewById(R.id.btnSelectDate);

        btnSelectDate.setOnClickListener(view -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (DatePicker dp, int y, int m, int d) -> {
                    // m is 0‑based; +1 for display
                    String dateText = d + "/" + (m + 1) + "/" + y;
                    btnSelectDate.setText(dateText);
                },
                year, month, day
        );

        dialog.show();
    }
}
