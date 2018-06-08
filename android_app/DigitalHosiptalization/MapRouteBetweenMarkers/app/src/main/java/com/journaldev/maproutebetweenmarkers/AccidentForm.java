package com.journaldev.maproutebetweenmarkers;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccidentForm extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, LocationListener {
    private static final int CAMERA_REQUEST = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_CAMERA = 1;
    LocationManager mylocation;
    String[] injury_types = {"Head Injury", "Fracture", "Heart Attack", "Suicide", "Pregnancy", "Collison",};
    private Bitmap bitmap;
    private File imageFile;
    private LocationManager locationManager;
    TextView locationText;
    ImageView image_preview;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    RequestQueue queue;
    double sourcelatitude, destlatitude;
    double sourcelongitude, destlongitude;

    private TextView ok;
    private boolean isProviderDisabled;
    AlertDialog alertDialog;
    private int CROP_IMAGE = 2;
    private Uri mImageCaptureUri;
    private Uri imageUri;
    private String selected_spinner_item;
    private EditText phone_numbere;
    private String mCurrentPhotoPath;
    private String basesixtyfourstring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_form);


        queue = Volley.newRequestQueue(this);
        locationText = (TextView) findViewById(R.id.location_txt);
        phone_numbere = (EditText) findViewById(R.id.phone_number);

        image_preview = (ImageView) findViewById(R.id.photo_preview);


        Spinner spin = (Spinner) findViewById(R.id.injury_type_sp);
        spin.setOnItemSelectedListener(this);

        Button take_photo_btn = (Button) findViewById(R.id.take_photo_btn);
        take_photo_btn.setOnClickListener(this);
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, injury_types);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);
        mylocation = (LocationManager) getSystemService(LOCATION_SERVICE);

        Button send_report_btn = (Button) findViewById(R.id.send_report_btn);
        send_report_btn.setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

            getLocation();
        }
// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(AccidentForm.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(AccidentForm.this,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(AccidentForm.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CAMERA);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.CAMERA}, 101);


        }
        getLocation();
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //listeners for spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selected_spinner_item = (String) parent.getItemAtPosition(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        if (resultCode == Activity.RESULT_OK) {


            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                image_preview.setVisibility(View.VISIBLE);

                Bitmap photo = (Bitmap) data.getExtras().get("data");
                image_preview.setImageBitmap(photo);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                basesixtyfourstring = encoded.replaceAll(" ", "$");


                     }


        }
    }





    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo_btn:
                System.out.println("Button clicked");

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                try {
                    imageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", createImageFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                break;
            case R.id.send_report_btn:
                System.out.println("Button clicked");
                //gettingNearestHispitals();
                try {
                    if(phone_numbere.getText().toString().length()==10&&locationText.getText().toString().length()>0) {
                        gettingNearestHospitalsPost();
                    }
                    else{
                        if(locationText.getText().toString().isEmpty()){
                            getLocation();
                        }
                        if(phone_numbere.getText().toString().isEmpty()){
                            phone_numbere.setError("Please enter mobile number.");
                        }
                      else if(phone_numbere.getText().toString().length()!=10){
                            phone_numbere.setError("Please enter valid mobile number.");

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.ok:
                alertDialog.dismiss();
                if (isProviderDisabled) {
                    if (locationText.getText() != "") {
                        Intent locintent = new Intent(AccidentForm.this, MapsActivity.class);
                        locintent.putExtra("sourcelatitude", sourcelatitude);
                        locintent.putExtra("sourcelongitude", sourcelongitude);
                        locintent.putExtra("destlatitude", destlatitude);
                        locintent.putExtra("destlongitude", destlongitude);
                        startActivity(locintent);
                    }
                } else {
                    getLocation();
                }
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    @Override
    public void onLocationChanged(Location location) {
        isProviderDisabled = true;
        sourcelatitude = location.getLatitude();
        sourcelongitude = location.getLongitude();
        //locationText.setText("Latitude: " +sourcelatitude + "\n Longitude: " + location.getLongitude());

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            locationText.setText(addresses.get(0).getAddressLine(0) + ", " +
                    addresses.get(0).getAddressLine(1) + ", " + addresses.get(0).getAddressLine(2));
        } catch (Exception e) {

        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        isProviderDisabled = false;
        Toast.makeText(AccidentForm.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    public void gettingNearestHispitals() {

        final String url = "http://rizwa.org/project.php";

// prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());


                        ShowpopupWindow(response);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

// add it to the RequestQueue
        queue.add(getRequest);
    }

    public void gettingNearestHospitalsPost() throws JSONException {




        final String URL = "http://13.127.142.166/get-nearest-hospital";
// Post params to be sent to the server
     /*   HashMap<String, String> params = new HashMap<String, String>();
        params.put("injury_type", selected_spinner_item);
        params.put("latitude", String.valueOf(sourcelatitude));
        params.put("longitude", String.valueOf(sourcelongitude));
        params.put("accident_image", basesixtyfourstring);
        params.put("accident_location", locationText.getText().toString());
        params.put("informer", phone_numbere.getText().toString());*/

        JSONObject json = new JSONObject();
        json.put("injury_type", selected_spinner_item);
        json.put("latitude", String.valueOf(sourcelatitude));
        json.put("longitude", String.valueOf(sourcelongitude));
        json.put("accident_image", basesixtyfourstring);
        json.put("accident_location", locationText.getText().toString());
        json.put("informer", phone_numbere.getText().toString());


        JsonObjectRequest request_json = new JsonObjectRequest(URL, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Process os success response
                        Log.d("onResponse", "onResponse: " + response);
                        ShowpopupWindow(response);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

// add the request object to the queue to be executed
        // add it to the RequestQueue
        queue.add(request_json);
    }

    private void ShowpopupWindow(JSONObject response) {
        try {
            LinearLayout hospital_desc;
            TextView hospital_namet, hospital_numbert, doctors_messaget;
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.response_popup, null);
            dialogBuilder.setView(dialogView);

            hospital_desc = (LinearLayout) dialogView.findViewById(R.id.hospital_desc);
            hospital_namet = (TextView) dialogView.findViewById(R.id.hospital_name);
            hospital_numbert = (TextView) dialogView.findViewById(R.id.hospital_number);
            doctors_messaget = (TextView) dialogView.findViewById(R.id.doctors_message);
            ok = (TextView) dialogView.findViewById(R.id.ok);
            ok.setOnClickListener(this);


            String mobno = response.getString("hospital_contact");
            /*"hospital_name"*/
            String hospital_name = response.getString("message");
            String doctors_message = response.getString("doctors_message");
            hospital_namet.setText(hospital_name);
            hospital_numbert.setText(mobno);
            doctors_messaget.setText(doctors_message);
            JSONObject hospital_location = response.getJSONObject("hospital_location");
            destlatitude = Double.parseDouble(hospital_location.getString("latitude"));
            destlongitude = Double.parseDouble(hospital_location.getString("longitude"));


            Button okbtn = (Button) dialogView.findViewById(R.id.okb);
            alertDialog = dialogBuilder.create();
            alertDialog.show();
            alertDialog.setCancelable(false);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        alertDialog.dismiss();
    }
}
