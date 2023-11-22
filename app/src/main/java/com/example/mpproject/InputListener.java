package com.example.mpproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// 터치(마우스) 이벤트를 구현하는 클래스

// 메인 게임뷰의 터치 리스너
class InputListener implements View.OnTouchListener {

    // 스와이프 및 이동 관련 상수 정의
    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_THRESHOLD_VELOCITY = 25;
    private static final int MOVE_THRESHOLD = 250;
    private static final int RESET_STARTING = 10;

    // 메인 뷰에 대한 참조
    private final MainView mView;

    // 게임 모드 참조를 위한 변수
    int gameMode = MainMenu.mGameMode; // 디폴트 : 0, 클린 : 1

    // 점수 참조를 위한 변수
    long score;

            // DB 접근 관련
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    // 터치 이벤트 처리를 위한 변수들
    private float x;
    private float y;
    private float lastDx; // 최근 x 변화량
    private float lastDy; // 최근 y 변화량
    private float previousX; // 이전 x 좌표
    private float previousY; // 이전 y 좌표
    private float startingX; // 시작 x 좌표
    private float startingY; // 시작 y 좌표

    // 이전 및 매우 이전 이동 방향
    private int previousDirection = 1;
    private int veryLastDirection = 1;

    // 셀 이동 여부 및 아이콘 선택 여부
    private boolean hasMoved = false;
    private boolean beganOnIcon = false;
    private Context context;


    // 생성자
    public InputListener(MainView view) {
        super();
        this.mView = view;
    }

    // 터치 이벤트 처리
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {

            // 터치 다운 이벤트
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                startingX = x;
                startingY = y;
                previousX = x;
                previousY = y;
                lastDx = 0;
                lastDy = 0;
                hasMoved = false;
                beganOnIcon = iconPressed(mView.sXNewGame, mView.sYIcons)
                        || iconPressed(mView.sXUndo, mView.sYIcons);
                return true;

            // 터치 이동 이벤트
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                if (mView.game.isActive() && !beganOnIcon) {
                    float dx = x - previousX;
                    if (Math.abs(lastDx + dx) < Math.abs(lastDx) + Math.abs(dx) && Math.abs(dx) > RESET_STARTING
                            && Math.abs(x - startingX) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastDx = dx;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDx == 0) {
                        lastDx = dx;
                    }
                    float dy = y - previousY;
                    if (Math.abs(lastDy + dy) < Math.abs(lastDy) + Math.abs(dy) && Math.abs(dy) > RESET_STARTING
                            && Math.abs(y - startingY) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastDy = dy;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDy == 0) {
                        lastDy = dy;
                    }
                    if (pathMoved() > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE && !hasMoved) {
                        boolean moved = false;
                        // 세로 이동
                        if (((dy >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dy) >= Math.abs(dx)) || y - startingY >= MOVE_THRESHOLD) && previousDirection % 2 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 2;
                            veryLastDirection = 2;
                            mView.game.move(2);
                        } else if (((dy <= -SWIPE_THRESHOLD_VELOCITY && Math.abs(dy) >= Math.abs(dx)) || y - startingY <= -MOVE_THRESHOLD) && previousDirection % 3 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 3;
                            veryLastDirection = 3;
                            mView.game.move(0);
                        }
                        // 가로 이동
                        if (((dx >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX >= MOVE_THRESHOLD) && previousDirection % 5 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 5;
                            veryLastDirection = 5;
                            mView.game.move(1);
                        } else if (((dx <= -SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX <= -MOVE_THRESHOLD) && previousDirection % 7 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 7;
                            veryLastDirection = 7;
                            mView.game.move(3);
                        }
                        if (moved) {
                            hasMoved = true;
                            startingX = x;
                            startingY = y;
                        }
                    }
                }
                previousX = x;
                previousY = y;
                return true;

            // 터치 업 이벤트
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                previousDirection = 1;
                veryLastDirection = 1;

                // "Menu" 입력 처리
                if (!hasMoved) {
                    if (iconPressed(mView.sXNewGame, mView.sYIcons)) {
                        if (!mView.game.gameLost()) {
                            // 게임이 진행 중이면 다이얼로그를 표시하여 리셋 여부를 확인
                            new AlertDialog.Builder(mView.getContext())
                                    .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mView.game.newGame();
                                        }
                                    })
                                    .setNegativeButton(R.string.continue_game, null)
                                    .setTitle(R.string.reset_dialog_title)
                                    .setMessage(R.string.reset_dialog_message)
                                    .show();
                        } else {
                            mView.game.newGame();
                        }
                    } else if (iconPressed(mView.sXUndo, mView.sYIcons)) {
                        // "Undo" 아이콘이 눌렸을 때 이전 상태로 되돌림
                        mView.game.revertUndoState();
                    }


                    // DB에 (닉네임, 게임모드, 점수) 등록
                    if (iconPressed(mView.sXRank, mView.sYIcons)) {

                        // AlertDialog.Builder에서 custom layout을 inflate
                        View rankMarginLayout = View.inflate(mView.getContext(), R.layout.rank_margin_layout, null);

                        // AlertDialog.Builder 생성
                        AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
                        builder.setView(rankMarginLayout);

                        // custom layout에서 EditText 찾기
                        EditText input = rankMarginLayout.findViewById(R.id.rankDialog);

                        builder.setPositiveButton(R.string.rank_dialog_insert, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 입력시, 게임모드, 닉네임, 점수 row 추가.
                                // DB open
                                myDBHelper myHelper = new myDBHelper(mView.getContext());
                                sqlDB = myHelper.getWritableDatabase();

                                // custom layout을 inflate해서 사용
                                View rankMarginLayout = View.inflate(mView.getContext(), R.layout.rank_margin_layout, null);

                                // 닉네임 읽기.
                                String name = input.getText().toString();

                                if (TextUtils.isEmpty(name)) {
                                    // 사용자가 아무것도 입력하지 않았을 때 처리
                                    Toast.makeText(mView.getContext(), "name을 입력하세요.", Toast.LENGTH_SHORT).show();
                                    return; // 이후 코드를 실행하지 않고 종료
                                }

                                // 문제1 : 이름 입력 안되는 버그 발견
                                // logcat 사용해서 확인해본 결과 -> 키보드를 끄고 등록하면 문제 없음....
                                // Error log : Ime callback not found. Ignoring unregisterReceivedCallback. callback
                                if (input != null) {
                                    Log.d("EditTextValue", "Input Value: " + name);
                                } else {
                                    Log.e("EditTextValue", "EditText not found");
                                }

                                // score 읽기. 등록직전 최신화
                                score = MainGame.enrollScore;

                                // SQL문 실행
                                String sqlQuery = "INSERT INTO scoreTBL (name, gamemode, score) VALUES ('" + name + "', " + gameMode + ", " + score + ");";
                                sqlDB.execSQL(sqlQuery);

                                // DB close
                                sqlDB.close();
                                Toast.makeText(mView.getContext(), "랭킹 등록완료 : " + name, Toast.LENGTH_SHORT).show();

                                dialog.dismiss();

                            }
                        });

                        builder.setNegativeButton(R.string.continue_game, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(mView.getContext(), "랭킹 등록취소", Toast.LENGTH_SHORT).show();

                                dialog.dismiss();
                            }
                        });

                        builder.setTitle(R.string.rank_dialog_title);
                        builder.setMessage(R.string.rank_dialog_message);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        // dialog 생성후, 키보드 닫아서 문제1 해결!
                        InputMethodManager imm = (InputMethodManager) mView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(alertDialog.getWindow().getDecorView().getWindowToken(), 0);
                    }
                    else if (isTap(2) && inRange(mView.startingX, x, mView.endingX)
                            && inRange(mView.startingY, y, mView.endingY) && mView.continueButtonEnabled) {
                        // "Continue" 버튼이 활성화되어 있고 화면의 특정 부분을 탭했을 때 처리 (해당 부분은 주석이 제공되지 않았습니다)
                    }
                }
        }
        return true;
    }

    // (x,y) 벡터의 절대값 반환
    private float pathMoved() {
        return (x - startingX) * (x - startingX) + (y - startingY) * (y - startingY);
    }

    // 아이콘 눌렸는지 검사
    private boolean iconPressed(int sx, int sy) {
        return isTap(1) && inRange(sx, x, sx + mView.iconSize)
                && inRange(sy, y, sy + mView.iconSize);
    }

    // check 가 a와 b 사이인지 확인
    private boolean inRange(float a, float check, float b) {
        return (a <= check && check <= b);
    }

    // factor를 눌렀는지 확인
    private boolean isTap(int factor) {
        return pathMoved() <= mView.iconSize * mView.iconSize * factor;
    }

    public static class myDBHelper extends SQLiteOpenHelper{
        public myDBHelper(Context context){
            super(context, "scoreDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE scoreTBL " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(40), gamemode INTEGER, " +
                    "score INTEGER, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS scoreTBL;");
            onCreate(db);
        }
    }
}
