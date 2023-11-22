package com.example.mpproject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/* MainView 클래스
수업시간에는 xml 레이아웃 파일을 이용해서 화면을 설계하고 xml을 java 코드로 조작하는 것을 배웠지만
저희가 만들 2048게임은 xml을 거의 이용하지 않고 java 파일에서 직접 그릴 예정입니다.
MainView클래스는 View 클래스를 상속하며, ondraw 메서드로 화면(canvas)을 직접 그리는 방식입니다.
저희가 만들 2048게임은 동적으로 ui를 제어하는 작업이 생각보다 많이 필요했습니다.. 그래서 반강제적으로 이 방법을 선택하게 되었습니다
이 방법의 장점, 이 방법을 선택한 이유는 구글링 해보시거나, gpt 돌리면 잘 알려줄 것 같습니다.
비트맵을 사용한 이유? 게임이나 다양한 상호작용을 필요로 하는 애플리케이션에서는 비트맵을 사용하여 그래픽 요소를 더 효율적으로 처리할 수 있음
비트맵을 사용하면 그래픽 처리가 효율적으로 이루어질 뿐만 아니라, UI 요소에 대한 복잡한 애니메이션 및 그래픽 효과를 구현하는 데도 도움이 됩니다.
게임에서 배경 이미지가 변경될 때마다 매번 리소스를 로드하는 것보다 한 번 비트맵에 그려놓고 필요할 때마다 그것을 그려 사용하는 것이 성능 면에서 유리할 수 있습니다.
코드 대부분이 그냥 자잘한 매서드들입니다. 그냥 그렇구나 하고 로직만 이해하시면 됩니다*/

public class MainView extends View {

    //내부 상수
    static final int BASE_ANI_TIME = 100000000;
    private static final String TAG = MainView.class.getSimpleName();
    private static final float MERGING_ACCELERATION = (float) -0.5;
    private static final float INITIAL_VELOCITY = (1 - MERGING_ACCELERATION) / 4;
    public final int numCellTypes = 21;
    private final BitmapDrawable[] bitmapCell = new BitmapDrawable[numCellTypes];
    public final MainGame game;

    //내부 변수들
    private final Paint paint = new Paint();
    public boolean hasSaveState = false;
    public boolean continueButtonEnabled = false;
    public int steps = 0;
    public int startingX;
    public int startingY;
    public int endingX;
    public int endingY;

    //아이콘 변수
    public int sYIcons;
    public int sXNewGame;
    public int sXUndo;
    public int sXRank;
    public int iconSize;

    //기타
    boolean refreshLastTime = true;
    boolean showHelp;

    //타이밍 체크
    private long lastFPSTime = System.nanoTime();

    //텍스트 크기
    private float titleTextSize;
    private float bodyTextSize;
    private float headerTextSize;
    private float instructionsTextSize;
    private float gameOverTextSize;

    //레이아웃 변수
    private int cellSize = 0;
    private float textSize = 0;
    private float cellTextSize = 0;
    private int gridWidth = 0;
    private int textPaddingSize;
    private int iconPaddingSize;

    //에셋
    private Drawable backgroundRectangle;
    private Drawable lightUpRectangle;
    private Drawable fadeRectangle;
    private Bitmap background = null;
    private BitmapDrawable loseGameOverlay;
    private BitmapDrawable winGameContinueOverlay;
    private BitmapDrawable winGameFinalOverlay;

    //텍스트 변수
    private int sYAll;
    private int titleStartYAll;
    private int bodyStartYAll;
    private int eYAll;
    private int titleWidthHighScore;
    private int titleWidthScore;

    public MainView(Context context) {
        super(context);

        Resources resources = context.getResources();
        //Loading resources
        game = new MainGame(context, this);
        try {
            // drawable에 있는 에셋 불러오기
            backgroundRectangle = resources.getDrawable(R.drawable.background_rectangle);
            lightUpRectangle = resources.getDrawable(R.drawable.cell_2048);
            fadeRectangle = resources.getDrawable(R.drawable.fade);
            if (MainMenu.mDarkMode) // 다크모드라면 어두운 배경
                this.setBackgroundColor(resources.getColor(R.color.background_dark));
            else // 아니면 흰 배경
                this.setBackgroundColor(resources.getColor(R.color.background));
            paint.setAntiAlias(true); // 안티엘리어싱, 계단현상 방지
        } catch (Exception e) {
            Log.e(TAG, "에셋 불러오기 에러 : ", e);
        }
        setOnTouchListener(new InputListener(this));
        game.newGame();
    }

    // 커스텀 로그함수, 0이 채워진 비트 수를 세어 뒤집음. 비트맵 배열에서 어떤 비트맵을 사용할 지 결정
    private static int log2(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    @Override
    public void onDraw(Canvas canvas) {

        // 배경 이미지를 그림
        canvas.drawBitmap(background, 0, 0, paint);

        // 현재 스코어를 표시하는 텍스트를 그림
        drawScoreText(canvas);

        // 게임이 종료되었고, 애니메이션이 활성화되지 않은 경우 리셋 버튼을 그림
        if (!game.isActive() && !game.aGrid.isAnimationActive()) {
            drawResetButton(canvas, true);
        }

        // 게임 보드의 각 셀을 그림
        drawCells(canvas);

        // 게임이 종료된 경우 (승리 또는 패배) 상태를 나타내는 UI를 그립니다.
        if (!game.isActive()) {
            drawEndGameState(canvas);
        }

        // 게임을 계속할 수 없는 경우 관련 텍스트
        if (!game.canContinue()) {
            drawEndlessText(canvas);
        }

        // 애니메이션이 활성화된 경우 애니메이션을 갱신하고 화면을 갱신
        if (game.aGrid.isAnimationActive()) {
            invalidate(startingX, startingY, endingX, endingY);
            tick();
        }
        // 게임이 활성 상태가 아니고, 마지막 갱신이 필요한 경우 화면을 갱신
        else if (!game.isActive() && refreshLastTime) {
            invalidate();
            refreshLastTime = false;
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        getLayout(width, height);
        createBitmapCells();
        createBackgroundBitmap(width, height);
        createOverlays();
    }

    // 바운드 치고 그리기
    private void drawDrawable(Canvas canvas, Drawable draw, int startingX, int startingY, int endingX, int endingY) {
        draw.setBounds(startingX, startingY, endingX, endingY);
        draw.draw(canvas);
    }

    // 셀 텍스트 그리기
    private void drawCellText(Canvas canvas, int value) { //  셀의 값과 캔버스를 매개변수로 받음
        if (MainMenu.mGameMode == 1) // 클린모드, 숫자를 표시하지 않고 바로 종료
            return ;
        int textShiftY = centerText(); // 텍스트 정렬
        if (value >= 8) { // 8일 기준으로 타일 색깔이 진해짐으로 8이상이면 흰색, 아니면 검은색
            paint.setColor(getResources().getColor(R.color.text_white));
        } else {
            paint.setColor(getResources().getColor(R.color.text_black));
        }
        canvas.drawText("" + value, cellSize / 2, cellSize / 2 - textShiftY, paint); // 캔버스에 숫자를 적고 정렬하고 색 바꿈
    }

    // 스코어 텍스트그리기
    private void drawScoreText(Canvas canvas) {
        //Drawing the score text: Ver 2
        paint.setTextSize(bodyTextSize);
        paint.setTextAlign(Paint.Align.CENTER);

        // 현재 점수의 텍스트 너비 계산
        int bodyWidthScore = (int) (paint.measureText("" + game.score));

        // 스코어 박스의 너비 계산
        int textWidthScore = Math.max(titleWidthScore, bodyWidthScore) + textPaddingSize * 2;

        // 스코어 박스의 가운데 위치 계산
        int textMiddleScore = textWidthScore / 2;

        int eXScore = endingX; // 스코어 박스의 종료 x 좌표
        int sXScore = endingX - textWidthScore; // 스코어 박스의 시작 x 좌표


        // 스코어 박스 그리기
        backgroundRectangle.setBounds(sXScore, sYAll, eXScore, eYAll);
        backgroundRectangle.draw(canvas);
        // 스코어 박스 안에 텍스트 그리기
        paint.setTextSize(titleTextSize);
        paint.setColor(getResources().getColor(R.color.text_white));
        canvas.drawText(getResources().getString(R.string.score), sXScore + textMiddleScore, titleStartYAll, paint);
        paint.setTextSize(bodyTextSize);
        paint.setColor(getResources().getColor(R.color.text_white));
        canvas.drawText(String.valueOf(game.score), sXScore + textMiddleScore, bodyStartYAll, paint);
    }

    // 리셋 버튼 그리기
    private void drawResetButton(Canvas canvas, boolean lightUp) {
        if (lightUp) { // 새로 시작해야할 때
            drawDrawable(canvas,
                    lightUpRectangle,
                    sXNewGame,
                    sYIcons,
                    sXNewGame + iconSize,
                    sYIcons + iconSize
            );
        } else {
            drawDrawable(canvas,
                    backgroundRectangle,
                    sXNewGame,
                    sYIcons, sXNewGame + iconSize,
                    sYIcons + iconSize
            );
        }
    }

    // 되돌리기 버튼 그리기
    private void drawUndoButton(Canvas canvas) {

        drawDrawable(canvas,
                backgroundRectangle,
                sXUndo,
                sYIcons, sXUndo + iconSize,
                sYIcons + iconSize
        );

        drawDrawable(canvas,
                getResources().getDrawable(R.drawable.ic_action_undo),
                sXUndo + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXUndo + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );
    }

    private void drawRankButton(Canvas canvas) {
        drawDrawable(canvas,
                backgroundRectangle,
                sXRank,
                sYIcons, sXRank + iconSize,
                sYIcons + iconSize
        );

        // 아이콘 이미지를 사용하거나 적절한 Drawable 리소스를 사용하여 버튼을 그립니다.
        drawDrawable(canvas,
                getResources().getDrawable(R.drawable.ic_action_ranking),
                sXRank + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXRank + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );
    }

    // 배경 그리기
    private void drawBackground(Canvas canvas) {
        drawDrawable(canvas, backgroundRectangle, startingX, startingY, endingX, endingY);
    }

    // 격자 배경 그리기
    private void drawBackgroundGrid(Canvas canvas) {
        Resources resources = getResources();
        Drawable backgroundCell = resources.getDrawable(R.drawable.cell);
        // Outputting the game grid
        for (int xx = 0; xx < game.numSquaresX; xx++) {
            for (int yy = 0; yy < game.numSquaresY; yy++) {
                int sX = startingX + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = startingY + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;

                drawDrawable(canvas, backgroundCell, sX, sY, eX, eY);
            }
        }
    }

    // 셀 그리기(애니메이션도 구현, 처리)
    // 이 부분이 중요합니다
    // 이 부분이 중요합니다
    // 이 부분이 중요합니다
    private void drawCells(Canvas canvas) {
        // 텍스트 크기 설정
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        // 각 셀을 순회
        for (int xx = 0; xx < game.numSquaresX; xx++) {
            for (int yy = 0; yy < game.numSquaresY; yy++) {
                // 현재 셀의 시작과 끝 좌표 계산
                int sX = startingX + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = startingY + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;

                // 현재 셀의 tile 객체 가져오기
                Tile currentTile = game.grid.getCellContent(xx, yy);
                if (currentTile != null) {
                    // 현재 타일의 값 및 그에 해당하는 비트맵 인덱스 구하기
                    int value = currentTile.getValue();
                    int index = log2(value);

                    // 현재 셀에 대한 애니메이션 정보 가져오기
                    ArrayList<AnimationCell> aArray = game.aGrid.getAnimationCell(xx, yy);
                    boolean animated = false;

                    // 애니메이션 정보를 역순으로 순회하며 처리
                    for (int i = aArray.size() - 1; i >= 0; i--) {
                        AnimationCell aCell = aArray.get(i);
                        // 만약 해당 애니메이션이 활성화되어 있다면 animated를 true로 설정
                        if (aCell.getAnimationType() == MainGame.SPAWN_ANI) {
                            animated = true;
                        }
                        // 만약 해당 애니메이션이 비활성화되어 있다면 다음으로 넘어감
                        if (!aCell.isActive()) {
                            continue;
                        }

                        // 스폰 애니메이션 처리
                        if (aCell.getAnimationType() == MainGame.SPAWN_ANI) { // Spawning animation
                            double percentDone = aCell.getPercentageDone();
                            float textScaleSize = (float) (percentDone);
                            paint.setTextSize(textSize * textScaleSize);

                            // 애니메이션이 진행될수록 텍스트 크기 및 셀 크기 조절하여 그림
                            float cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                            bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                            bitmapCell[index].draw(canvas);
                        }
                        // 머지 애니메이션 처리
                            else if (aCell.getAnimationType() == MainGame.MERGE_ANI) { // Merging Animation
                            double percentDone = aCell.getPercentageDone();
                            float textScaleSize = (float) (1 + INITIAL_VELOCITY * percentDone
                                    + MERGING_ACCELERATION * percentDone * percentDone / 2);
                            // 애니메이션이 진행될수록 텍스트 크기 및 셀 크기 조절하여 그림
                            paint.setTextSize(textSize * textScaleSize);
                            float cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                            bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                            bitmapCell[index].draw(canvas);
                        }
                        // 무브 애니메이션 처리
                            else if (aCell.getAnimationType() == MainGame.MOVE_ANI) {  // Moving animation
                            double percentDone = aCell.getPercentageDone();
                            int tempIndex = index;
                            // 머지 애니메이션이 있을 경우 인덱스 조절
                            if (aArray.size() >= 2) {
                                tempIndex = tempIndex - 1;
                            }

                            // 이전 위치와 현재 위치를 기반으로 이동 거리 계산
                            int previousX = aCell.extras[0];
                            int previousY = aCell.extras[1];
                            int currentX = currentTile.getX();
                            int currentY = currentTile.getY();
                            int dX = (int) ((currentX - previousX) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                            int dY = (int) ((currentY - previousY) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                            // 이동 거리를 기반으로 비트맵 위치 설정하여 그림
                            try{
                                bitmapCell[tempIndex].setBounds(sX + dX, sY + dY, eX + dX, eY + dY);
                                bitmapCell[tempIndex].draw(canvas);}
                            catch(NullPointerException e){Log.i("MainView",e + " : at 348");}
                        }
                        // 애니메이션이 활성화되었음을 표시
                        animated = true;
                    }

                    // 활성화된 애니메이션이 없을 경우 그냥 셀을 그림
                    if (!animated) {
                        bitmapCell[index].setBounds(sX, sY, eX, eY);
                        bitmapCell[index].draw(canvas);
                    }
                }
            }
        }
    }

    // 게임 끝났을때 (승리 or 패배) 그리기
    private void drawEndGameState(Canvas canvas) {
        double alphaChange = 1;
        continueButtonEnabled = false;
        for (AnimationCell animation : game.aGrid.globalAnimation) {
            if (animation.getAnimationType() == MainGame.FADE_GLOBAL_ANI) {
                alphaChange = animation.getPercentageDone();
            }
        }
        BitmapDrawable displayOverlay = null;
        if (game.gameWon()) {
            if (game.canContinue()) {
                continueButtonEnabled = true;
                displayOverlay = winGameContinueOverlay;
            } else {
                displayOverlay = winGameFinalOverlay;
            }
        } else if (game.gameLost()) {
            displayOverlay = loseGameOverlay;
        }

        if (displayOverlay != null) {
            displayOverlay.setBounds(startingX, startingY, endingX, endingY);
            displayOverlay.setAlpha((int) (255 * alphaChange));
            displayOverlay.draw(canvas);
        }
    }

    // 2048 넘을때
    private void drawEndlessText(Canvas canvas) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(bodyTextSize);
        paint.setColor(getResources().getColor(R.color.text_black));
        canvas.drawText(getResources().getString(R.string.continue_game), startingX, sYIcons - centerText() * 2, paint);
    }

    // 게임 끝났을때 (승리 or 패배) 그리기
    private void createEndGameStates(Canvas canvas, boolean win, boolean showButton) {
        int width = endingX - startingX;
        int length = endingY - startingY;
        int middleX = width / 2;
        int middleY = length / 2;
        if (win) {
            lightUpRectangle.setAlpha(127);
            drawDrawable(canvas, lightUpRectangle, 0, 0, width, length);
            lightUpRectangle.setAlpha(255);
            paint.setColor(getResources().getColor(R.color.text_white));
            paint.setAlpha(255);
            paint.setTextSize(gameOverTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            int textBottom = middleY - centerText();
            canvas.drawText(getResources().getString(R.string.you_win), middleX, textBottom, paint);
            paint.setTextSize(bodyTextSize);
        } else {
            fadeRectangle.setAlpha(127);
            drawDrawable(canvas, fadeRectangle, 0, 0, width, length);
            fadeRectangle.setAlpha(255);
            paint.setColor(getResources().getColor(R.color.text_black));
            paint.setAlpha(255);
            paint.setTextSize(gameOverTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getResources().getString(R.string.game_over), middleX, middleY - centerText(), paint);
        }
    }

    // 배경 비트맵 그리기
    private void createBackgroundBitmap(int width, int height) {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        drawResetButton(canvas, false);
        drawUndoButton(canvas);
        drawRankButton(canvas);
        drawBackground(canvas);
        drawBackgroundGrid(canvas);
    }

    // 셀 비트맵 그리기
    private void createBitmapCells() {
        Resources resources = getResources();
        int[] cellRectangleIds = getCellRectangleIds();
        paint.setTextAlign(Paint.Align.CENTER);
        for (int xx = 1; xx < bitmapCell.length; xx++) {
            int value = (int) Math.pow(2, xx);
            if (xx == 20 && MainMenu.mGameMode == 1) value = 0;
            paint.setTextSize(cellTextSize);
            float tempTextSize = cellTextSize * cellSize * 0.9f / Math.max(cellSize * 0.9f, paint.measureText(String.valueOf(value)));
            paint.setTextSize(tempTextSize);
            Bitmap bitmap = Bitmap.createBitmap(cellSize, cellSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawDrawable(canvas, resources.getDrawable(cellRectangleIds[xx]), 0, 0, cellSize, cellSize);
            drawCellText(canvas, value);
            bitmapCell[xx] = new BitmapDrawable(resources, bitmap);
        }
    }

    // 셀 drawable 배열 구축
    private int[] getCellRectangleIds() {
        int[] cellRectangleIds = new int[numCellTypes];
        cellRectangleIds[0] = R.drawable.cell;
        cellRectangleIds[1] = R.drawable.cell_2;
        cellRectangleIds[2] = R.drawable.cell_4;
        cellRectangleIds[3] = R.drawable.cell_8;
        cellRectangleIds[4] = R.drawable.cell_16;
        cellRectangleIds[5] = R.drawable.cell_32;
        cellRectangleIds[6] = R.drawable.cell_64;
        cellRectangleIds[7] = R.drawable.cell_128;
        cellRectangleIds[8] = R.drawable.cell_256;
        cellRectangleIds[9] = R.drawable.cell_512;
        cellRectangleIds[10] = R.drawable.cell_1024;
        cellRectangleIds[11] = R.drawable.cell_2048;
        for (int xx = 12; xx < cellRectangleIds.length; xx++) {
            cellRectangleIds[xx] = R.drawable.cell_4096;
        }
        return cellRectangleIds;
    }

    // 오버레이 초기화
    private void createOverlays() {
        Resources resources = getResources();
        //Initialize overlays
        Bitmap bitmap = Bitmap.createBitmap(endingX - startingX, endingY - startingY, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        createEndGameStates(canvas, true, true);
        winGameContinueOverlay = new BitmapDrawable(resources, bitmap);
        bitmap = Bitmap.createBitmap(endingX - startingX, endingY - startingY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        createEndGameStates(canvas, true, false);
        winGameFinalOverlay = new BitmapDrawable(resources, bitmap);
        bitmap = Bitmap.createBitmap(endingX - startingX, endingY - startingY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        createEndGameStates(canvas, false, false);
        loseGameOverlay = new BitmapDrawable(resources, bitmap);
    }

    // 한 틱 체크
    private void tick() {
        long currentTime = System.nanoTime();
        game.aGrid.tickAll(currentTime - lastFPSTime);
        lastFPSTime = currentTime;
    }

    // 틱 업데이트
    public void resyncTime() {
        lastFPSTime = System.nanoTime();
    }

    // 레이아웃 가져오기
    private void getLayout(int width, int height) {
        cellSize = Math.min(width / (game.numSquaresX + 1), height / (game.numSquaresY + 3));
        gridWidth = cellSize / 7;
        int screenMiddleX = width / 2;
        int screenMiddleY = height / 2;
        int boardMiddleY = screenMiddleY + cellSize / 2;
        iconSize = cellSize / 2;

        //Grid Dimensions
        double halfNumSquaresX = game.numSquaresX / 2d;
        double halfNumSquaresY = game.numSquaresY / 2d;
        startingX = (int) (screenMiddleX - (cellSize + gridWidth) * halfNumSquaresX - gridWidth / 2);
        endingX = (int) (screenMiddleX + (cellSize + gridWidth) * halfNumSquaresX + gridWidth / 2);
        startingY = (int) (boardMiddleY - (cellSize + gridWidth) * halfNumSquaresY - gridWidth / 2);
        endingY = (int) (boardMiddleY + (cellSize + gridWidth) * halfNumSquaresY + gridWidth / 2);

        float widthWithPadding = endingX - startingX;

        // Text Dimensions
        paint.setTextSize(cellSize);
        textSize = cellSize * cellSize / Math.max(cellSize, paint.measureText("0000"));

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(1000);
        instructionsTextSize = Math.min(
                1000f * (widthWithPadding / (paint.measureText(getResources().getString(R.string.instructions)))),
                textSize / 1.5f
        );
        gameOverTextSize = Math.min(
                Math.min(
                        1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.game_over)))),
                        textSize * 2
                ),
                1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.you_win))))
        );

        paint.setTextSize(cellSize);
        cellTextSize = textSize;
        titleTextSize = textSize / 3;
        bodyTextSize = (int) (textSize / 1.5);
        headerTextSize = textSize * 2;
        textPaddingSize = (int) (textSize / 3);
        iconPaddingSize = (int) (textSize / 5);

        paint.setTextSize(titleTextSize);

        int textShiftYAll = centerText();
        //static variables
        sYAll = (int) (startingY - cellSize * 1.5);
        titleStartYAll = (int) (sYAll + textPaddingSize + titleTextSize / 2 - textShiftYAll);
        bodyStartYAll = (int) (titleStartYAll + textPaddingSize + titleTextSize / 2 + bodyTextSize / 2);

        titleWidthHighScore = (int) (paint.measureText(getResources().getString(R.string.high_score)));
        titleWidthScore = (int) (paint.measureText(getResources().getString(R.string.score)));
        paint.setTextSize(bodyTextSize);
        textShiftYAll = centerText();
        eYAll = (int) (bodyStartYAll + textShiftYAll + bodyTextSize / 2 + textPaddingSize);

        sYIcons = (startingY + eYAll) / 2 - iconSize / 2;
        sXNewGame = (endingX - iconSize);
        sXUndo = sXNewGame - iconSize * 3 / 2 - iconPaddingSize;
        sXRank = sXUndo - iconSize * 3 / 2 - iconPaddingSize;
        resyncTime();
    }

    private int centerText() {
        return (int) ((paint.descent() + paint.ascent()) / 2);
    }

}
