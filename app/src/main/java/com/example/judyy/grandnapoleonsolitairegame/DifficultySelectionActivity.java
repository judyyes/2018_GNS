package com.example.judyy.grandnapoleonsolitairegame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

public class DifficultySelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty_selection);
    }

    protected void onClickStartGame0(View v){
        startActivity(new Intent(this, GameActivity.class));
    }


}
