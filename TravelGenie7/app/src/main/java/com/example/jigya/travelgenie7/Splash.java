package com.example.jigya.travelgenie7;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("register",0);
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        Thread mythread=new Thread(){
            public void run(){
                try {
                    sleep(3000);

                    if(sharedPreferences.getBoolean("registered",false))
                    {
                        Intent i=new Intent(getApplicationContext(),MainActivityN.class);
                        startActivity(i);
                    }
                    else
                    {
                        Intent i=new Intent(getApplicationContext(),registerActivity.class);
                        startActivity(i);
                    }


                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        mythread.start();
    }
}
