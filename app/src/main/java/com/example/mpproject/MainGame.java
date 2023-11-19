package com.example.mpproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainGame {

    /*애니메이션 변수*/
    static final int SPAWN_ANI = -1;
    static final int MOVE_ANI = 0;
    static final int MERGE_ANI = 1;
    static final int FADE_GLOBAL_ANI = 0;
    private static final long MOVE_ANI_TIME = MainView.BASE_ANI_TIME;
    private static final long SPAWN_ANI_TIME = MainView.BASE_ANI_TIME;
    private static final long NOTIFICATION_DELAY_TIME = MOVE_ANI_TIME + SPAWN_ANI_TIME;
    private static final long NOTIFICATION_ANI_TIME = MainView.BASE_ANI_TIME * 5;

    private static final int startingMaxValue = 2048; // 제한값

    /* 게임 상태 변수 */
    private static final int GAME_WIN = 1;
    private static final int GAME_LOST = -1;
    private static final int GAME_NORMAL = 0;
    int gameMode = MainMenu.mGameMode;
    int gameState = GAME_NORMAL;
    int lastGameState = GAME_NORMAL;
    private int bufferGameState = GAME_NORMAL;

    private static final int GAME_ENDLESS = 2;
    private static final int GAME_ENDLESS_WON = 3;
    private static final String FIRST_RUN = "first run";
    private static int endingMaxValue;

    // 내부 변수
    final int numSquaresX = 4;
    final int numSquaresY = 4;
    private final Context mContext;
    private final MainView mView;
    Grid grid = null;
    AnimationGrid aGrid;
    boolean canUndo;
    public long score = 0;
    long highScore = 0;
    long lastScore = 0;
    private long bufferScore = 0;

    MainGame(Context context, MainView view) { // MainGame 객체 초기화
        mContext = context;
        mView = view;
        endingMaxValue = (int) Math.pow(2, view.numCellTypes - 1);
    }

    // 새 게임 시작하기
    void newGame() {
        if (grid == null) {
            grid = new Grid(numSquaresX, numSquaresY);
        } else {
            prepareUndoState();
            saveUndoState();
            grid.clearGrid();
        }
        score = 0;
        aGrid = new AnimationGrid(numSquaresX, numSquaresY);
        gameState = GAME_NORMAL;
        addStartTiles();
        mView.showHelp = firstRun();
        mView.refreshLastTime = true;
        mView.resyncTime();
        mView.invalidate();
    }

    // 시작 타일 초기화
    private void addStartTiles() {
        int startTiles = 2;
        for (int xx = 0; xx < startTiles; xx++) {
            this.addRandomTile();
        }
    }

    // 랜덤 타일 놓기
    private void addRandomTile() {
        if (grid.isCellsAvailable()) {
            int value;
            value = Math.random() < 0.9 ? 2 : 4; // 4 생성확률 10%
            Tile tile = new Tile(grid.randomAvailableCell(), value);
            spawnTile(tile);
        }
    }

    // 타일 생성
    private void spawnTile(Tile tile) {
        grid.insertTile(tile);
        aGrid.startAnimation(tile.getX(), tile.getY(), SPAWN_ANI,
                SPAWN_ANI, MOVE_ANI, null); //Direction: -1 = EXPANDING
    }


    // 초기화
    private boolean firstRun() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (settings.getBoolean(FIRST_RUN, true)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FIRST_RUN, false);
            editor.apply();
            return true;
        }
        return false;
    }

    // 타일 준비
    private void prepareTiles() {
        for (Tile[] array : grid.field) {
            for (Tile tile : array) {
                if (grid.isCellOccupied(tile)) {
                    tile.setMergedFrom(null);
                }
            }
        }
    }

    // 타일 이동
    private void moveTile(Tile tile, Cell cell) {
        grid.field[tile.getX()][tile.getY()] = null;
        grid.field[cell.getX()][cell.getY()] = tile;
        tile.updatePosition(cell);
    }

    // 되돌리기를 위한 상태 저장
    private void saveUndoState() {
        grid.saveTiles();
        canUndo = true;
        lastScore = bufferScore;
        lastGameState = bufferGameState;
    }

    // 되돌리기를 위한 상태 준비
    private void prepareUndoState() {
        grid.prepareSaveTiles();
        bufferScore = score;
        bufferGameState = gameState;
    }

    // 되돌리기
    void revertUndoState() {
        if (canUndo) {
            canUndo = false;
            aGrid.cancelAnimations();
            grid.revertTiles();
            score = lastScore;
            gameState = lastGameState;
            mView.refreshLastTime = true;
            mView.invalidate();
        }
    }

    // 게임 승리 여부 체크
    boolean gameWon() {
        return (gameState > 0 && gameState % 2 != 0);
    }

    // 게임 패배 여부 체크
    boolean gameLost() {
        return (gameState == GAME_LOST);
    }

    // 게임이 진행중인가
    boolean isActive() {
        return !(gameWon() || gameLost());
    }

    // 타일 이동
    void move(int direction) {
        aGrid.cancelAnimations();
        // 0: up, 1: right, 2: down, 3: left
        if (!isActive()) {
            return;
        }
        prepareUndoState();
        Cell vector = getVector(direction);
        List<Integer> traversalsX = buildXTraverse(vector);
        List<Integer> traversalsY = buildYTraverse(vector);
        boolean moved = false; // 움직였는가

        prepareTiles();

        // 격자 내 모든 타일을 돌면서, 기준 타일과 새 타일의 충돌 처리 해줌
        for (int xx : traversalsX) {
            for (int yy : traversalsY) {
                Cell cell = new Cell(xx, yy);
                Tile tile = grid.getCellContent(cell);

                if (tile != null) {
                    Cell[] positions = findFarthestPosition(cell, vector);
                    Tile next = grid.getCellContent(positions[1]);

                    if (next != null && (next.getValue() == tile.getValue() || next.getValue() == 0 || tile.getValue() == 0) && next.getMergedFrom() == null) {
                        Tile merged = new Tile(positions[1], tile.getValue() * 2);
                        Tile[] temp = {tile, next};
                        merged.setMergedFrom(temp);

                        if (merged.getValue() != 0 && next.getValue() != 0)
                            grid.insertTile(merged);
                        else
                            grid.removeTile(next);
                        grid.removeTile(tile);


                        // 두 타일의 위치를 커버함
                        tile.updatePosition(positions[1]);

                        int[] extras = {xx, yy};
                        aGrid.startAnimation(merged.getX(), merged.getY(), MOVE_ANI,
                                MOVE_ANI_TIME, 0, extras); //방향: 0 = MOVING MERGED
                        aGrid.startAnimation(merged.getX(), merged.getY(), MERGE_ANI,
                                SPAWN_ANI_TIME, MOVE_ANI_TIME, null);

                        // 점수 업데이트
                        score = score + merged.getValue();
                        highScore = Math.max(score, highScore);

                        // 2048 만들었으면
                        if (merged.getValue() >= winValue() && !gameWon()) {
                            gameState = gameState + GAME_WIN; // 이겼다고 표시
                            endGame();

                        }
                    } else {
                        moveTile(tile, positions[0]);
                        int[] extras = {xx, yy, 0};
                        aGrid.startAnimation(positions[0].getX(), positions[0].getY(), MOVE_ANI, MOVE_ANI_TIME, 0, extras); //방향: 1 = MOVING NO MERGE
                    }

                    if (!positionsEqual(cell, tile)) {
                        moved = true;
                    }


                }
            }
        }

        if (moved) {
            saveUndoState();
            addRandomTile();
            checkLose();
        }
        mView.resyncTime();
        mView.invalidate();
        MainActivity.startSound();
    }

    // 패배했는지 체크
    private void checkLose() {
        if (!movesAvailable() && !gameWon()) {
            gameState = GAME_LOST;
            endGame();
        }
    }

    // 게임 종료
    private void endGame() {
        aGrid.startAnimation(-1, -1, FADE_GLOBAL_ANI, NOTIFICATION_ANI_TIME, NOTIFICATION_DELAY_TIME, null);
    }

    // 기준 벡터 반환 (크기가 1인 동서남북 벡터)
    private Cell getVector(int direction) {
        Cell[] map = {
                new Cell(0, -1), // up
                new Cell(1, 0),  // right
                new Cell(0, 1),  // down
                new Cell(-1, 0)  // left
        };
        return map[direction];
    }

    // 셀을 X축 기준으로 가져오기
    private List<Integer> buildXTraverse(Cell vector) {
        List<Integer> traversals = new ArrayList<>();

        for (int xx = 0; xx < numSquaresX; xx++) {
            traversals.add(xx);
        }
        if (vector.getX() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    // 셀을 Y축 기준으로 가져오기
    private List<Integer> buildYTraverse(Cell vector) {
        List<Integer> traversals = new ArrayList<>();

        for (int xx = 0; xx < numSquaresY; xx++) {
            traversals.add(xx);
        }
        if (vector.getY() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    // 셀이나 벽을 만날때 까지의 위치를 찾음
    private Cell[] findFarthestPosition(Cell cell, Cell vector) {
        Cell previous;
        Cell nextCell = new Cell(cell.getX(), cell.getY());
        do {
            previous = nextCell;
            nextCell = new Cell(previous.getX() + vector.getX(),
                    previous.getY() + vector.getY());
        } while (grid.isCellWithinBounds(nextCell) && grid.isCellAvailable(nextCell));

        return new Cell[]{previous, nextCell};
    }

    // 이동가능한지 체크
    private boolean movesAvailable() {
        return grid.isCellsAvailable() || tileMatchesAvailable();
    }

    // 만나서 없어질 타일인지 체크
    private boolean tileMatchesAvailable() {
        Tile tile;

        for (int xx = 0; xx < numSquaresX; xx++) {
            for (int yy = 0; yy < numSquaresY; yy++) {
                tile = grid.getCellContent(new Cell(xx, yy));

                if (tile != null) {
                    for (int direction = 0; direction < 4; direction++) {
                        Cell vector = getVector(direction);
                        Cell cell = new Cell(xx + vector.getX(), yy + vector.getY());

                        Tile other = grid.getCellContent(cell);

                        if (other != null && other.getValue() == tile.getValue()) {
                            return true;
                        }
                        if (other != null && (other.getValue() == 0 || tile.getValue() == 0)){
                            //둘중 하나 0일때도 체크해야함
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // 두 셀이 같은 위치인가
    private boolean positionsEqual(Cell first, Cell second) {
        return first.getX() == second.getX() && first.getY() == second.getY();
    }

    private int winValue() {
        if (!canContinue()) {
            return endingMaxValue;
        } else {
            return startingMaxValue;
        }
    }

    // 게임을 지속할 수 있는지
    boolean canContinue() {
        return !(gameState == GAME_ENDLESS || gameState == GAME_ENDLESS_WON);
    }
}

