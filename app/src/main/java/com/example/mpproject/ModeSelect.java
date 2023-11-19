package com.example.mpproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

// 게임모드 고르는 액티비티 (시작 버튼 후)
public class ModeSelect extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modeselect);

        // 모드 설정
        final String[] modes = {"기본 4x4 모드", "클린 모드 (숫자 표기 X)"};

        ListView lv = findViewById(R.id.Gamemode);

        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, modes);
        lv.setAdapter(ad);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 사용자가 선택한 모드에 따라 MainMenu의 mGameMode 설정
                switch (position) { // 이 부분에서 게임모드 추가하시면 됩니다.
                    case 0:
                        MainMenu.mGameMode = 0; // 기본 4x4 모드
                        break;
                    case 1:
                        MainMenu.mGameMode = 1; // 클린 모드 (숫자 표기 X)
                        break;
                }
                startActivity(new Intent(ModeSelect.this, MainActivity.class));
            }
        });
    }
}
