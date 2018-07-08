package com.example.jigya.travelgenie7;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class registerActivity extends AppCompatActivity {
    MyDBHelper mydb;

    EditText text1;
    EditText text2;
    EditText text3;
    //MyDBHelper help;

    Button buttonupdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mydb=new MyDBHelper(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        text1=(EditText)findViewById(R.id.uno);
        text2=findViewById(R.id.p1);
        text3=findViewById(R.id.p2);
        //buttonupdate=(Button)findViewById(R.id.update);
        //help=new MyDBHelper (this,"",null,1);
        //Update();
    }
    public void onRegisterClick(View view)
    {
        MyDBHelper db=new MyDBHelper(this);
        try
        {
            if(db.add(text1.getText().toString(),
                    text2.getText().toString(),text3.getText().toString()))
            {
                displayMsg("Registration Successful");
                SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("register",0);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("registered",true);
                editor.commit();
                Intent inten=new Intent(getApplicationContext(),MainActivityN.class);
                startActivity(inten);
            }
        }
        catch (Exception e)
        {
            displayMsg("Enter valid data");
        }
        text1.setText("");
        text2.setText("");
        text3.setText("");
    }

    /*public  void onDisplayClick(View view)
    {
        Intent display=new Intent(this,Disp.class);
        startActivity(display);

    }*/
    private void displayMsg(String s)
    {
        Context context=getApplicationContext();
        int duration= Toast.LENGTH_SHORT;
        Toast toast=Toast.makeText(context,s,duration);
        toast.show();
    }
   /* public void Update(){
        buttonupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isupdate=mydb.update(text1.getText().toString(),text2.getText().toString(),text3.getText().toString());
                if(isupdate==true)
                    Toast.makeText(MainActivity.this,"Data Updated",Toast.LENGTH_LONG
                    ).show();
            }
        });
    }*/
}