package com.example.mpproject;

import java.util.ArrayList; // 가변 선형 리스트

public class Grid {

    // 현재 그리드
    public final Tile[][] field;
    // 취소 그리드
    public final Tile[][] undoField;
    // 임시 저장 그리드
    private final Tile[][] bufferField;

    // 그리드 생성자
    public Grid(int sizeX, int sizeY) {
        field = new Tile[sizeX][sizeY];
        undoField = new Tile[sizeX][sizeY];
        bufferField = new Tile[sizeX][sizeY];
        clearGrid(); // 그리드 초기화
        clearUndoGrid(); // 취소 그리드 초기화
    }

    // 랜덤으로 셀이 생성될 수 있는 자리를 반환하는 메서드
    public Cell randomAvailableCell() {
        ArrayList<Cell> availableCells = getAvailableCells();
        if (availableCells.size() >= 1) {
            return availableCells.get((int) Math.floor(Math.random() * availableCells.size()));
        }
        return null;
    }

    // 현재 비어있는 자리를 반환해서 리스트에 추가하는 메서드
    private ArrayList<Cell> getAvailableCells() {
        ArrayList<Cell> availableCells = new ArrayList<>();
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] == null) {
                    availableCells.add(new Cell(xx, yy));
                }
            }
        }
        return availableCells;
    }

    // 그리드에 셀을 더 놓을 수 있는지 확인
    public boolean isCellsAvailable() {
        return (getAvailableCells().size() >= 1);
    }

    // 특정 셀이 비어있는지 확인
    public boolean isCellAvailable(Cell cell) {
        return !isCellOccupied(cell);
    }

    // 특정 셀이 이미 차있는지 확인
    public boolean isCellOccupied(Cell cell) {
        return (getCellContent(cell) != null);
    }

    // 특정 셀의 타일 가져오기
    public Tile getCellContent(Cell cell) {
        if (cell != null && isCellWithinBounds(cell)) { //셀의 유효성 판단
            return field[cell.getX()][cell.getY()]; // 유효한 셀인 경우, 해당 셀의 x,y 좌표를 이용하여 field 배열에서 타일을 가져와 반환(주어진 셀에 존재하는 타일 객체 얻음)
        } else {
            return null;
        }
    }

    // 좌표에 해당하는 타일 가져오기
    public Tile getCellContent(int x, int y) {
        if (isCellWithinBounds(x, y)) {
            return field[x][y];
        } else {
            return null;
        }
    }

    // 셀의 좌표가 유효한지 확인
    public boolean isCellWithinBounds(Cell cell) {
        return 0 <= cell.getX() && cell.getX() < field.length
                && 0 <= cell.getY() && cell.getY() < field[0].length;
    }

    // 좌표가 유효한지 확인
    private boolean isCellWithinBounds(int x, int y) {
        return 0 <= x && x < field.length
                && 0 <= y && y < field[0].length;
    }

    // 타일을 격자에 삽입
    public void insertTile(Tile tile) {
        field[tile.getX()][tile.getY()] = tile;
    }

    // 타일을 격자에서 제거
    public void removeTile(Tile tile) {
        field[tile.getX()][tile.getY()] = null;
    }

    // 타일 저장 (되돌리기 용)
    public void saveTiles() {
        for (int xx = 0; xx < bufferField.length; xx++) {
            for (int yy = 0; yy < bufferField[0].length; yy++) {
                if (bufferField[xx][yy] == null) {
                    undoField[xx][yy] = null;
                } else {
                    undoField[xx][yy] = new Tile(xx, yy, bufferField[xx][yy].getValue());
                }
            }
        }
    }

    // 되돌리기 준비(버퍼에 저장)
    public void prepareSaveTiles() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] == null) {
                    bufferField[xx][yy] = null;
                } else {
                    bufferField[xx][yy] = new Tile(xx, yy, field[xx][yy].getValue());
                }
            }
        }
    }

    // 되돌리기
    public void revertTiles() {
        for (int xx = 0; xx < undoField.length; xx++) {
            for (int yy = 0; yy < undoField[0].length; yy++) {
                if (undoField[xx][yy] == null) {
                    field[xx][yy] = null;
                } else {
                    field[xx][yy] = new Tile(xx, yy, undoField[xx][yy].getValue());
                }
            }
        }
    }

    // 그리드 비우기
    public void clearGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                field[xx][yy] = null;
            }
        }
    }

    // 취소 그리드 비우기
    private void clearUndoGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                undoField[xx][yy] = null;
            }
        }
    }
}