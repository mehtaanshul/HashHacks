package com.example.root.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.root.myapp.BleActivity;
import com.example.root.myapp.ClassicBluetoothActivity;
import com.example.root.myapp.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnCbt = (Button) findViewById(R.id.btn_cbt_activity);
        Button btnBle = (Button) findViewById(R.id.btn_ble_activity);

        btnCbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ClassicBluetoothActivity.class));
            }
        });

        btnBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BleActivity.class));
            }
        });
    }
}

