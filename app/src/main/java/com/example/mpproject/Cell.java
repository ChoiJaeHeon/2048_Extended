package com.example.mpproject;

// 기본 셀(좌표)
public class Cell
{
    private int x; // 현재 객체의 x좌표
    private int y; // 현재 객체의 y좌표

    public Cell(int x, int y)
    {
        this.x = x; // 전달받은 x 값으로 초기화
        this.y = y; // 전달받은 y 값으로 초기화
    }

    // 아래는 get set 메서드
    public int getX()
    {
        return this.x;
    }

    void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return this.y;
    }

    void setY(int y)
    {
        this.y = y;
    }
}
