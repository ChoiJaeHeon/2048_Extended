package com.example.mpproject;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// 옵션 뷰 설정, 다크모드와 사운드를 설정 할 수 있음
public class Option extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        ;
        Switch darkModeSwitch = findViewById(R.id.Darkmode); // xml에서 각각 연결
        Switch soundSwitch = findViewById(R.id.Sound);

        darkModeSwitch.setChecked(MainMenu.mDarkMode);
        soundSwitch.setChecked(MainMenu.mSound);

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) { // 다크모드 토글
                if (isChecked){
                    MainMenu.mDarkMode = true;
                    Toast.makeText(getApplicationContext(), // 토스트 메시지
                            "DarkMode ON", Toast.LENGTH_SHORT).show();
                }
                else{
                    MainMenu.mDarkMode = false;
                    Toast.makeText(getApplicationContext(), // 토스트 메시지
                            "DarkMode OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { // 소리 토글
                if (isChecked) {
                    MainMenu.mSound = true;
                    Toast.makeText(getApplicationContext(), // 토스트 메시지
                            "Sound ON", Toast.LENGTH_SHORT).show();
                } else {
                    MainMenu.mSound = false;
                    Toast.makeText(getApplicationContext(), // 토스트 메시지
                            "Sound OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok:
                finish();
                break;
        }
    }


}