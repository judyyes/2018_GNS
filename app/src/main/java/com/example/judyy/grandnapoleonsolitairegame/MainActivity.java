package com.example.judyy.grandnapoleonsolitairegame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

//     Navigate to difficulty selection page onClick
    protected void onClickDifficultySelection(View v){
        startActivity(new Intent(this, DifficultySelectionActivity.class));
    }


}
