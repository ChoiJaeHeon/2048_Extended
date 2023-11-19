package com.example.mpproject;

// 타일 클래스 (셀->타일->그리드)
// 셀 = 좌표, 배경 이미지를 담고있음. 즉 ui 부분을 담당
// 타일 = 각 셀에 값(2의 제곱수)을 가지고 담겨있는 타일, 셀을 상속받아서 위치를 가짐, 즉 논리적인 부분을 담당
// 그리드 = tile 객체들을 담고있는 2차원배열(게임보드)
public class Tile extends Cell {
    private final int value; // 2의 제곱수, 합쳐지면서 계속 증가
    private Tile[] mergedFrom = null; // 합쳐졌을 경우, 어떤 타일들이 합쳐진건지 저장하는 배열, 초기에는 null

    public Tile(int x, int y, int value) { // tile 생성자
        super(x, y);
        this.value = value;
    }

    public Tile(Cell cell, int value) { // tile 생성자
        super(cell.getX(), cell.getY());
        this.value = value;
    }

    public void updatePosition(Cell cell) { // 타일 위치 업데이트
        this.setX(cell.getX());
        this.setY(cell.getY());
    }

    public int getValue() { // 현재 타일의 값
        return this.value;
    }

    public Tile[] getMergedFrom() { // 원래 타일이 담겨있는 배열을 반환
        return mergedFrom;
    }

    public void setMergedFrom(Tile[] tile) { // 원래 타일을 mergedfrom에 저장(덮어쓰기)
        mergedFrom = tile;
    }
}
