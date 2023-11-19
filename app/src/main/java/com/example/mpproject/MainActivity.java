package com.example.mpproject;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity
{
    private static final String WIDTH = "width"; // 그리드의 너비
    private static final String HEIGHT = "height"; // 그리드의 높이
    private static final String SCORE = "score"; // 현재 게임의 점수
    private static final String HIGH_SCORE = "high score temp"; // 최고 점수를 일시적으로 저장, 게임이 종료될 때 현재 최고 점수보다 높은 점수가 있을 경우에만 최고 점수를 업데이트
    private static final String UNDO_SCORE = "undo score"; // 되돌리기 직전의 점수 저장
    private static final String CAN_UNDO = "can undo"; // 되돌리기 가능 여부 체크
    private static final String UNDO_GRID = "undo"; // 되돌리기
    private static final String GAME_STATE = "game state"; // 현재 게임 상태, 게임 진행중, 게임 오버
    private static final String UNDO_GAME_STATE = "undo game state"; // 되돌리기 직전의 게임 상태 저장

    //효과음 변수
    public static SoundPool sound;
    public static int soundId;
    public static int streamId;

    private MainView view; // MainView 클래스로 인스턴스를 만들고 MainActivity에서 접근

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // 화면에 보여질 레이아웃(activity_game) 설정

        //프레임 레이아웃 : 이 위에 canvas 겹쳐 그리기 => MainView
        FrameLayout frameLayout = findViewById(R.id.game_frame_layout); // activity_game.xml에서 id가 game_frame_layout인 framelayout을 저장
        view = new MainView(this); // MainView(게임을 그리는데 사용될 뷰) 클래스의 인스턴스를 생성

        // 저장되있던 값이 있다면 불러오기
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        view.hasSaveState = settings.getBoolean("save_state", false); // 저장된 상태가 있는지 확인
        if (savedInstanceState != null) // 있으면 덮어쓰기
            if (savedInstanceState.getBoolean("hasState"))
                load(); // 게임상태 복원하는 메서드


        //view 의 레이아웃 파라미터 설정 -> framelayout(부모레이아웃)과 상호작용 하게 하기 위함. 이를통해 부모 레이아웃에서의 ㅐ
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        frameLayout.addView(view); //FrameLayout에 MainView를 추가, MainView는 game_frame_layout 위에 겹쳐져 그려지게 됩

        sound = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        soundId = sound.load(this, R.raw.beep,1);
    }

    // 효과음 재생
    public static void startSound(){
        if (MainMenu.mSound) {
            streamId = sound.play(soundId, 0.5f, 0.5f, 1, 0, 1);
        }
    }

    // 키다운 이벤트 처리
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU)
            return true;
        else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
        {
            view.game.move(2);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
        {
            view.game.move(0);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
        {
            view.game.move(3);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
        {
            view.game.move(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 상태 저장
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putBoolean("hasState", true);
        save();
        super.onSaveInstanceState(savedInstanceState);
    }

    // 일시 정지
    protected void onPause()
    {
        super.onPause();
        save();
    }

    // 필드와 상태 저장
    private void save()
    {
        final int mode = MainMenu.mGameMode;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        Tile[][] field = view.game.grid.field;
        Tile[][] undoField = view.game.grid.undoField;
        editor.putInt(WIDTH + mode, field.length);
        editor.putInt(HEIGHT + mode, field.length);

        for (int xx = 0; xx < field.length; xx++)
        {
            for (int yy = 0; yy < field[0].length; yy++)
            {
                if (field[xx][yy] != null)
                    editor.putInt(mode + " " + xx + " " + yy, field[xx][yy].getValue());
                else
                    editor.putInt(mode + " " + xx + " " + yy, 0);

                if (undoField[xx][yy] != null)
                    editor.putInt(UNDO_GRID + mode + " " + xx + " " + yy, undoField[xx][yy].getValue());
                else
                    editor.putInt(UNDO_GRID + mode + " " + xx + " " + yy, 0);
            }
        }

        // SharedPreferences에 들어갈 값들, PreferenceManager가 관리함
        editor.putLong(SCORE + mode, view.game.score);
        editor.putLong(UNDO_SCORE + mode, view.game.lastScore);
        editor.putBoolean(CAN_UNDO + mode, view.game.canUndo);
        editor.putInt(GAME_STATE + mode, view.game.gameState);
        editor.putInt(UNDO_GAME_STATE + mode, view.game.lastGameState);
        editor.apply();
    }

    // 필드와 상태 불러오기
    private void load()
    {
        final int mode = MainMenu.mGameMode;

        //Stopping all animations
        view.game.aGrid.cancelAnimations();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        for (int xx = 0; xx < view.game.grid.field.length; xx++)
        {
            for (int yy = 0; yy < view.game.grid.field[0].length; yy++)
            {
                int value = settings.getInt( mode + " " + xx + " " + yy, -1);
                if (value > 0)
                    view.game.grid.field[xx][yy] = new Tile(xx, yy, value);
                else if (value == 0)
                    view.game.grid.field[xx][yy] = null;

                int undoValue = settings.getInt(UNDO_GRID + mode + " " + xx + " " + yy, -1);
                if (undoValue > 0)
                    view.game.grid.undoField[xx][yy] = new Tile(xx, yy, undoValue);
                else if (value == 0)
                    view.game.grid.undoField[xx][yy] = null;
            }
        }

        view.game.score = settings.getLong(SCORE + mode, view.game.score);
        view.game.highScore = settings.getLong(HIGH_SCORE + mode, view.game.highScore);
        view.game.lastScore = settings.getLong(UNDO_SCORE + mode, view.game.lastScore);
        view.game.canUndo = settings.getBoolean(CAN_UNDO + mode, view.game.canUndo);
        view.game.gameState = settings.getInt(GAME_STATE + mode, view.game.gameState);
        view.game.lastGameState = settings.getInt(UNDO_GAME_STATE + mode, view.game.lastGameState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);
    }

}