package com.eldar.srm;

import android.Manifest;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class SRM_Main extends AppCompatActivity
{
    private final int INTERNET_REQUEST_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_srm__main);
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED)
        {
            getPermissions();
        }

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(onClickListener);
    }
    private void getPermissions()
    {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},INTERNET_REQUEST_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INTERNET_REQUEST_RESULT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){}
                else
                {
                    this.finishAffinity();
                }
                return;
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.button:
                {
                    DictionaryLoader dl = new DictionaryLoader();
                    dl.createLocalDict("https://sites.google.com/site/neocennuznyjsajt/fajly/sample_dict.txt");
                    break;
                }
                case R.id.button2:
                    break;
                case R.id.button3:
                    break;
            }

        }
    };




}
