package com.example.mpproject;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mpproject.R;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private RecyclerView rankingRecyclerView;
    private RankingAdapter rankingAdapter;
    private List<RankingItem> rankingList;
    private int rankGameMode = 0; // 기본적으로 모드 0을 선택

    InputListener.myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        rankingList = new ArrayList<>();
        rankingAdapter = new RankingAdapter(rankingList);

        rankingRecyclerView = findViewById(R.id.rankingRecyclerView);
        rankingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rankingRecyclerView.setAdapter(rankingAdapter);

        // SQLite 데이터베이스로부터 랭킹 데이터를 가져오는 함수 호출
        loadRankingData();

        // 무한 스크롤 리스너 추가
        rankingRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // 리스트의 맨 끝에 도달하면 새로운 데이터를 가져오도록 로직을 추가
                if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    loadMoreData(); // 새로운 데이터 가져오는 메서드 호출
                }
            }
        });

        // 버튼 클릭 리스너 등록
        findViewById(R.id.ButtonMode0).setOnClickListener(this::onModeButtonClick);
        findViewById(R.id.ButtonMode1).setOnClickListener(this::onModeButtonClick);
        findViewById(R.id.ButtonMode2).setOnClickListener(this::onModeButtonClick);
    }

    private void loadRankingData() {
        myHelper = new InputListener.myDBHelper(this);
        sqlDB = myHelper.getReadableDatabase();

        // 쿼리 수행
        String[] projection = {
                "id",
                "name",
                "score",
        };

        String selection = "gamemode = ?";
        String[] selectionArgs = {String.valueOf(rankGameMode)};

        Cursor cursor = sqlDB.query(
                "scoreTBL",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                "score DESC, created_at ASC"
        );

        // 결과 처리
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String playerName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int score = cursor.getInt(cursor.getColumnIndexOrThrow("score"));

                // RankingItem 객체 생성 및 리스트에 추가
                RankingItem rankingItem = new RankingItem(id, playerName, rankGameMode, score, "");
                rankingList.add(rankingItem);
            } while (cursor.moveToNext());

            // 어댑터 갱신
            rankingAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "랭킹 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        // 리소스 해제
        if (cursor != null) {
            cursor.close();
        }

        // sqlDB.close(); // 주석 처리: sqlDB를 닫지 않도록 변경
    }

    private void loadMoreData() {
        // 더 많은 데이터를 가져오는 로직을 여기에 구현
        // 여기에서는 간단히 isLoading을 true로 설정하고, 잠시 후에 false로 변경하는 예시를 보여줍니다.

        isLoading = true;

        // 이후에 데이터를 추가로 가져오는 쿼리 실행

        // 예시: 1초 후에 isLoading을 false로 변경하여 더 이상 데이터를 가져오지 않도록 함
        rankingRecyclerView.postDelayed(() -> {
            isLoading = false;
            Log.d("RankingActivity", "Loading more data");
        }, 1000);
    }

    private void onModeButtonClick(View view) {
        switch (view.getId()) {
            case R.id.ButtonMode0:
                rankGameMode = 0;
                break;
            case R.id.ButtonMode1:
                rankGameMode = 1;
                break;
            case R.id.ButtonMode2:
                rankGameMode = 2;
                break;
        }

        // 새로운 모드에 대한 랭킹을 로드
        rankingList.clear(); // 기존 데이터 초기화
        loadRankingData();
    }
}