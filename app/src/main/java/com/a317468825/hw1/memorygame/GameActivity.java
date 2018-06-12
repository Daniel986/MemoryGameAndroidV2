package com.a317468825.hw1.memorygame;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import tyrantgit.explosionfield.ExplosionField;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private int numOfElements;
    private GameButton[] buttons;
    private int[] buttonGraphicLocation;
    private int[] buttonGraphics;
    private GameButton selectedButton1;
    private GameButton selectedButton2;
    private TextView textName;

    private boolean isBusy = false;

    private int pairedNum = 0;
    private final Handler handler = new Handler();
    private long time;
    private CountDownTimer timer;

    private String name;
    private int age;

    private GridLayout grid;
    private ExplosionField mExplosionField;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private SensorEventListener gyroscopeEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        Bundle extrasBundle = this.getIntent().getExtras();
        grid = (GridLayout) findViewById(R.id.game_grid);

        int numCols = extrasBundle.getInt("columns");
        int numRows = extrasBundle.getInt("rows");
        time = (long) extrasBundle.getInt("time");
        name = extrasBundle.getString("name");
        age = extrasBundle.getInt("age");


        textName = (TextView) findViewById(R.id.name_container_text);
        textName.setText(name);

        grid.setColumnCount(numCols);
        grid.setRowCount(numRows);

        numOfElements = numCols * numRows;

        buttons = new GameButton[numOfElements];
        buttonGraphics = new int[numOfElements / 2];

        loadGraphics();

        buttonGraphicLocation = new int[numOfElements];

        shuffleButtonGraphics();

        // fill grid with buttons
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                GameButton tempButton = new GameButton(this, row, col, buttonGraphics[buttonGraphicLocation[(row * numCols) + col]], numOfElements);
                tempButton.setId(View.generateViewId());
                tempButton.setOnClickListener(this);
                buttons[(row * numCols) + col] = tempButton;
                grid.addView(tempButton);
            }
        }

        mExplosionField = ExplosionField.attach2Window(this);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(gyroscopeSensor == null) {
            Toast.makeText(this, getString(R.string.no_gyro_text), Toast.LENGTH_LONG).show();
        }

        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(IsTilted(sensorEvent)) {
                    // Flip last pair
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);

        //start timer
        countDownStart();

    }

    private boolean IsTilted(SensorEvent sensorEvent) {
        for(int i = 0; i < sensorEvent.values.length; i++)
        if(sensorEvent.values[i] > 0.5f || sensorEvent.values[i] < -0.5f) {
            String str = "Moved in " + i + " axis\nValue : " + sensorEvent.values[i];
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroscopeEventListener);
    }

    private void countDownStart() {
        timer = new CountDownTimer((long) time * 1000, 1000) {

            TextView timer = (TextView) findViewById(R.id.seconds_left_text);

            public void onTick(long millisUntilFinished) {
                if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) == 10)
                    timer.setTextColor(Color.RED);
                timer.setText(" " + TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished));
            }

            public void onFinish() {
                timer.setText("HALT!!!");
                Toast.makeText(GameActivity.this, getString(R.string.loser_text), Toast.LENGTH_LONG).show();

                mExplosionField.explode(textName);
                mExplosionField.explode((TextView)findViewById(R.id.seconds_left_text));

                TransitionManager.beginDelayedTransition(grid,makeExplodeTransition());
                toggleVisibility(buttons);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        returnToMenu();
                    }
                }, 2000);
            }
        }.start();
    }

    private void returnToMenu() {
        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("age", age);
        startActivity(intent);
    }

    private void loadGraphics() {
        buttonGraphics[0] = R.drawable.button_1;
        buttonGraphics[1] = R.drawable.button_2;
        if (numOfElements > 4) {
            buttonGraphics[2] = R.drawable.button_3;
            buttonGraphics[3] = R.drawable.button_4;
            buttonGraphics[4] = R.drawable.button_5;
            buttonGraphics[5] = R.drawable.button_6;
            buttonGraphics[6] = R.drawable.button_7;
            buttonGraphics[7] = R.drawable.button_8;
        }
        if (numOfElements > 16) {
            buttonGraphics[8] = R.drawable.button_9;
            buttonGraphics[9] = R.drawable.button_10;
            buttonGraphics[10] = R.drawable.button_11;
            buttonGraphics[11] = R.drawable.button_12;
        }
    }


    protected void shuffleButtonGraphics() {
        Random rand = new Random();

        for (int i = 0; i < numOfElements; i++) {
            buttonGraphicLocation[i] = i % (numOfElements / 2);
        }

        for (int i = 0; i < numOfElements; i++) {
            int temp = buttonGraphicLocation[i];
            int swapIdx = rand.nextInt(numOfElements);
            buttonGraphicLocation[i] = buttonGraphicLocation[swapIdx];
            buttonGraphicLocation[swapIdx] = temp;
        }

    }


    @Override
    public void onClick(View view) {

        if (isBusy)
            return;

        GameButton button = (GameButton) view;

        if (button.isMatched())
            return;

        if (selectedButton1 == null) {
            selectedButton1 = button;
            selectedButton1.flip();
            return;
        }

        if (selectedButton1.getId() == button.getId())
            return;

        if (selectedButton1.getFrontImageDrawableId() == button.getFrontImageDrawableId()) {
            button.flip();
            selectedButton1.setMatched(true);
            selectedButton1.setEnabled(false);
            button.setEnabled(false);
            selectedButton1 = null;

            pairedNum++;
            checkIfWon();
            return;
        } else {
            selectedButton2 = button;
            selectedButton2.flip();
            isBusy = true;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectedButton2.flip();
                    selectedButton1.flip();
                    selectedButton1 = null;
                    selectedButton2 = null;
                    isBusy = false;
                }
            }, 500);
        }
    }

    private void checkIfWon() {
        if (pairedNum == buttonGraphics.length) {
            timer.cancel();
            Toast.makeText(this, getString(R.string.winner_text),
                    Toast.LENGTH_LONG).show();

            mExplosionField.explode(textName);
            mExplosionField.explode((TextView)findViewById(R.id.seconds_left_text));
            mExplosionField.explode((TextView)findViewById(R.id.time_text));

            TransitionManager.beginDelayedTransition(grid,makeFadeTransition());
            toggleVisibility(buttons);


            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    returnToMenu();
                }
            }, 2000);
        }
    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        returnToMenu();
    }


    private Explode makeExplodeTransition(){
        Explode explode = new Explode();
        explode.setDuration(2000);
        explode.setInterpolator(new AnticipateOvershootInterpolator());
        return explode;
    }

    private Fade makeFadeTransition(){
        Fade fade = new Fade();
        fade.setDuration(2000);
        fade.setInterpolator(new AccelerateInterpolator());
        return fade;
    }

    // Custom method to toggle visibility of views
    private void toggleVisibility(View... views){
        // Loop through the views
        for(View v: views){
            if(v.getVisibility()==View.VISIBLE){
                v.setVisibility(View.INVISIBLE);
            }else {
                v.setVisibility(View.VISIBLE);
            }
        }
    }
}
