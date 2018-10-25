package com.example.judyy.grandnapoleonsolitairegame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

public class DifficultySelectionActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.judyy.grandnapoleonsolitairegame.TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_difficulty_selection);
    }

    public void onClickStartGame0(View v){
        Intent normalMode = new Intent(this, GameActivity.class);
        normalMode.putExtra(EXTRA_MESSAGE, "normal");
        startActivity(normalMode);
    }

    public void onClickStartGame1(View v){
        Intent dummyMode = new Intent(this, GameActivity.class);
        dummyMode.putExtra(EXTRA_MESSAGE, "dummy");
        startActivity(dummyMode);
    }


}
