package com.example.mpproject;

import java.util.ArrayList;

// 애니메이션 격자를 구현하기 위한 클래스
public class AnimationGrid {
    // 전역 애니메이션 리스트
    public final ArrayList<AnimationCell> globalAnimation = new ArrayList<>();

    // 격자 배열
    private final ArrayList<AnimationCell>[][] field;

    // 활성화된 애니메이션 수
    private int activeAnimations = 0;

    // 추가 프레임 여부
    private boolean moreFrames = false;

    // 생성자 & 초기화
    public AnimationGrid(int x, int y) {
        field = new ArrayList[x][y];

        // 격자 초기화
        for (int xx = 0; xx < x; xx++) {
            for (int yy = 0; yy < y; yy++) {
                field[xx][yy] = new ArrayList<>();
            }
        }
    }

    // 애니메이션 핸들링 시작
    public void startAnimation(int x, int y, int animationType, long length, long delay, int[] extras) {
        // 셀에 애니메이션 추가
        AnimationCell animationToAdd = new AnimationCell(x, y, animationType, length, delay, extras);
        if (x == -1 && y == -1) {
            // 전역 애니메이션에 추가
            globalAnimation.add(animationToAdd);
        } else {
            // 특정 위치의 셀에 추가
            field[x][y].add(animationToAdd);
        }
        activeAnimations = activeAnimations + 1;
    }

    // 애니메이션 재생
    public void tickAll(long timeElapsed) {
        // 종료된 애니메이션을 저장할 리스트
        ArrayList<AnimationCell> cancelledAnimations = new ArrayList<>();

        // 전역 애니메이션 처리
        for (AnimationCell animation : globalAnimation) {
            animation.tick(timeElapsed);
            if (animation.animationDone()) {
                cancelledAnimations.add(animation);
                activeAnimations = activeAnimations - 1;
            }
        }

        // 격자의 모든 셀 애니메이션 처리
        for (ArrayList<AnimationCell>[] array : field) {
            for (ArrayList<AnimationCell> list : array) {
                for (AnimationCell animation : list) {
                    animation.tick(timeElapsed);
                    if (animation.animationDone()) {
                        cancelledAnimations.add(animation);
                        activeAnimations = activeAnimations - 1;
                    }
                }
            }
        }

        // 종료된 애니메이션을 취소
        for (AnimationCell animation : cancelledAnimations) {
            cancelAnimation(animation);
        }
    }

    // 애니메이션 활성화 여부 체크
    public boolean isAnimationActive() {
        if (activeAnimations != 0) {
            moreFrames = true;
            return true;
        } else if (moreFrames) {
            moreFrames = false;
            return true;
        } else {
            return false;
        }
    }

    // 특정 위치의 셀에 대한 애니메이션 리스트 반환
    public ArrayList<AnimationCell> getAnimationCell(int x, int y) {
        return field[x][y];
    }

    /* 애니메이션 취소 */
    public void cancelAnimations() {
        // 전체 애니메이션 취소
        for (ArrayList<AnimationCell>[] array : field) {
            for (ArrayList<AnimationCell> list : array) {
                list.clear();
            }
        }
        globalAnimation.clear();
        activeAnimations = 0;
    }

    // 특정 애니메이션 취소
    private void cancelAnimation(AnimationCell animation) {
        if (animation.getX() == -1 && animation.getY() == -1) {
            // 전역 애니메이션에서 취소
            globalAnimation.remove(animation);
        } else {
            // 특정 위치의 셀 애니메이션에서 취소
            field[animation.getX()][animation.getY()].remove(animation);
        }
    }
}
