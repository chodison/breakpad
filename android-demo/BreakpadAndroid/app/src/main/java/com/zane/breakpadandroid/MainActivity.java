package com.zane.breakpadandroid;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.chodison.mybreakpad.ExceptionHandler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ExceptionHandler.init(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public void doClick(View view){

        ExceptionHandler.testNativeCrash();

    }
}
