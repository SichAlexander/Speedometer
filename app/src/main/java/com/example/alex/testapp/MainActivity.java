package com.example.alex.testapp;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private GaugeView mGaugeView;
    private boolean isRunning = false;
    private Button btnStart;
    private final Random RAND = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findUI();
        setListener();
        showStatus();

    }

    private void setListener() {
        btnStart.setOnClickListener(this);
    }

    private void findUI() {
        mGaugeView  = (GaugeView) findViewById(R.id.speedometer_gauge_view_ActivityMain);
        btnStart    =(Button) findViewById(R.id.start_btn_ActivityMain);
    }

    private final CountDownTimer mTimer = new CountDownTimer(30000, 1000) {

        @Override
        public void onTick(final long millisUntilFinished) {
            mGaugeView.setTargetValue(RAND.nextInt(50) + 10);

        }

        @Override
        public void onFinish() {}
    };



    private void showStatus(){
        btnStart.setText(isRunning ? getString(R.string.stop) : getString(R.string.start));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_btn_ActivityMain){
            changeStatus();
        }
    }
    private void changeStatus(){
        isRunning = !isRunning;
        if (isRunning){
            mTimer.start();
        } else {
            mTimer.cancel();
        }
        showStatus();
    }
}
