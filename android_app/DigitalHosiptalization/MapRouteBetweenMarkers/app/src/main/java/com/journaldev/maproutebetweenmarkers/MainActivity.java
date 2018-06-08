package com.journaldev.maproutebetweenmarkers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageButton call_ambulance_btn;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    Boolean b[] = new Boolean[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        call_ambulance_btn = (ImageButton) findViewById(R.id.call_ambulance_btn);
        b[0] = false;
        b[1] = false;
        call_ambulance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(b[0]==true&&b[1]==true){
                        Intent load_accident_form = new Intent(MainActivity.this, AccidentForm.class);
                        startActivity(load_accident_form);
                    }else{
                        requestPermissions(new String[]{
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                },
                                REQUEST_ID_MULTIPLE_PERMISSIONS);
                    }

                } else {
                    Intent load_accident_form = new Intent(MainActivity.this, AccidentForm.class);
                    startActivity(load_accident_form);


                }
            }

        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                    },
                    REQUEST_ID_MULTIPLE_PERMISSIONS);

        } else {
            Intent load_accident_form = new Intent(MainActivity.this, AccidentForm.class);
            startActivity(load_accident_form);


        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                try {
                    int i = 0;
                    for (String permission : permissions) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            //denied
                            Log.e("denied" + i, permission);

                            Toast.makeText(MainActivity.this, "denied our app." + i, Toast.LENGTH_LONG).show();

                            b[i] = false;
                        } else {
                            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                                //allowed
                                Log.e("allowed", permission);

                                b[i] = true;
                                i++;

                            } else {



                                b[i] = false;
                            }
                        }
                    }
                   if(b[0]==true&&b[1]==true){

                    }else{
                        ShowpopupWindow();
                    }


                } catch (Exception e) {
                }
                break;
        }

    }

    private void ShowpopupWindow() {
        try {

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.popup_layout, null);
            dialogBuilder.setView(dialogView);
            Button okbtn = (Button) dialogView.findViewById(R.id.okb);
            final AlertDialog alertDialog = dialogBuilder.create();
             alertDialog.show();
            alertDialog.setCancelable(false);


            okbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

