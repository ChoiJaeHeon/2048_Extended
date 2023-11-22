package com.example.mpproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public class MainMenu extends AppCompatActivity {
    public static boolean mIsMainMenu = true;
    public static boolean mDarkMode = false;
    public static boolean mSound = true;

    public static int mGameMode = 0; //0 : default, 1: clean


    private final String BACKGROUND_COLOR_KEY = "BackgroundColor";
    public static int mBackgroundColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

        mIsMainMenu = true;

        Button startButton = findViewById(R.id.Start);
        Button exitButton = findViewById(R.id.Exit);
        Button shareButton = findViewById(R.id.share);
        Button rankButton = findViewById(R.id.Rank);

        startButton.setOnClickListener(ButtonClickListener);
        exitButton.setOnClickListener(ButtonClickListener);
        shareButton.setOnClickListener(ButtonClickListener);
        rankButton.setOnClickListener(ButtonClickListener);

    }

    // 버튼 클릭 리스너
    // 시작버튼 눌렀을떄, 종료 버튼 눌렀을떄, 공유하기 눌렀을때로 나뉨
    public View.OnClickListener ButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Start:
                    mIsMainMenu = false;
                    startActivity(new Intent(MainMenu.this, ModeSelect.class));
                    break;
                case R.id.Exit:
                    ActivityCompat.finishAffinity(MainMenu.this);
                    System.exit(0);
                    break;
                case R.id.Rank:
                    mIsMainMenu = false;
                    startActivity(new Intent(MainMenu.this, RankingActivity.class));
                    break;
                case R.id.share: // 메시지와 플레이 스토어 링크를 공유할수 있음
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String playStoreLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "2048 게임 하쉴?? \n" + playStoreLink);; // 메시지에 링크 추가
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.app_name))); // 공유 인텐트 추가
                    break;
            }
        }
    };

    // 옵션 메뉴 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    // 옵션이 선택 되었을 때 (이벤트 처리)
    public boolean onOptionsItemSelected (MenuItem item)
    {
        Toast toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG);

        switch(item.getItemId())
        {
            case R.id.settingmenu:
                startActivity(new Intent(MainMenu.this, Option.class));
                break;
            case R.id.info:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle("2023 Mobile Programming Second Project")
                        .setMessage("2048 Extended by 3분반 13조")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog alertDialog = b.create();
                alertDialog.show();
                break;
        }

        toast.show();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

}