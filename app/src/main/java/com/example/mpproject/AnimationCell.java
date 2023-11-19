package com.example.mpproject;

// 셀 애니메이션을 구현하기 위한 클래스
class AnimationCell extends Cell {
    public final int[] extras; // 추가 매개 변수를 저장하는 배열
    private final int animationType; // 애니메이션의 유형을 지정하는 정수 값
    private final long animationTime; // 애니메이션의 총 지속 시간(밀리초)
    private final long delayTime; // 애니메이션을 시작하기 전의 대기 시간(밀리초)
    private long timeElapsed; // 애니메이션 실행 중 경과한 총 시간(밀리초)

    // 생성자: AnimationCell 객체 초기화
    public AnimationCell(int x, int y, int animationType, long length, long delay, int[] extras) {
        super(x, y); // Cell 클래스의 생성자 호출
        this.animationType = animationType;
        animationTime = length;
        delayTime = delay;
        this.extras = extras;
    }

    // 애니메이션의 유형을 반환 (병합, 이동, 스폰)
    public int getAnimationType() {
        return animationType;
    }

    // 애니메이션의 경과 시간을 업데이트
    public void tick(long timeElapsed) {
        this.timeElapsed = this.timeElapsed + timeElapsed;
    }

    // 애니메이션이 완료되었는지 여부를 확인
    public boolean animationDone() {
        return animationTime + delayTime < timeElapsed;
    }

    // 애니메이션이 얼마나 진행되었는지 백분율로 반환 (애니메이션의 속도를 조절하기 위함)
    public double getPercentageDone() {
        return Math.max(0, 1.0 * (timeElapsed - delayTime) / animationTime);
    }

    // 애니메이션이 활성화되었는지 여부를 반환
    public boolean isActive() {
        return (timeElapsed >= delayTime);
    }
}
