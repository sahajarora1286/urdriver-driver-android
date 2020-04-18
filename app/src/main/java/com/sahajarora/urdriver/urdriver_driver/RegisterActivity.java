package com.sahajarora.urdriver.urdriver_driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.sahajarora.urdriver.urdriver_driver.helper.SQLiteHandler;
import com.sahajarora.urdriver.urdriver_driver.helper.SessionManager;
import com.urdriver.sahajarora.myapplication.backend.driverApi.DriverApi;
import com.urdriver.sahajarora.myapplication.backend.driverApi.model.Driver;
import com.urdriver.sahajarora.myapplication.backend.userApi.UserApi;
import com.urdriver.sahajarora.myapplication.backend.userApi.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class    RegisterActivity extends AppCompatActivity implements Serializable {
    private Button btnRegister;
    private EditText etName, etAddress, etPostalCode, etPhone, etUsername, etPassword, etConfirmPassword,
                    etLicenseNumber, etDrivingExperience;
    private TextView tvTandC;
    private CheckBox cbTandC;
    private static final String TAG = RegisterActivity.class.getSimpleName();
    public static boolean registration_successful;


    private static ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // getActionBar().setDisplayHomeAsUpEnabled(true);


        registration_successful = false;
        etName = (EditText) findViewById(R.id.etName);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etPostalCode = (EditText) findViewById(R.id.etPostalCode);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);
        etLicenseNumber = (EditText) findViewById(R.id.etLicenseNumber);
        etDrivingExperience = (EditText) findViewById(R.id.etDrivingExperience);

        cbTandC = (CheckBox) findViewById(R.id.cbTandC);

        tvTandC = (TextView) findViewById(R.id.tvTandC);

        String udata="Terms and Conditions";
        SpannableString content = new SpannableString(udata);
        content.setSpan(new UnderlineSpan(), 0, udata.length(), 0);//where first 0 shows the starting and udata.length() shows the ending span.if you want to span only part of it than you can change these values like 5,8 then it will underline part of it.
        tvTandC.setText(content);
        tvTandC.setTextColor(Color.BLUE);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    HomeActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String email = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String postalCode = etPostalCode.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String licenseNumber = etLicenseNumber.getText().toString().trim();


                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || postalCode.isEmpty() || phone.isEmpty() ||
                        etConfirmPassword.getText().length() == 0 ||
                        etLicenseNumber.getText().length() == 0 || etDrivingExperience.getText().length()==0) {
                    Toast.makeText(getApplicationContext(), "Please fill in all the fields!", Toast.LENGTH_LONG).show();
                } else if (!password.equals(etConfirmPassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match!", Toast.LENGTH_LONG).show();
                } else if (!cbTandC.isChecked()){
                    AlertDialog.Builder ad = new AlertDialog.Builder(getApplicationContext());
                    ad.setMessage("Please read the terms and conditions.");
                    ad.setPositiveButton("OK", null);
                    ad.show();
                }

                else {
                    int drivingExperience = Integer.parseInt(etDrivingExperience.getText().toString().trim());

                    Driver driver = new Driver();
                    driver.setName(name);
                    driver.setEmail(email);
                    driver.setPassword(password);
                    driver.setAddress(address);
                    driver.setPostalCode(postalCode);
                    driver.setPhone(phone);
                    driver.setLicenseNumber(licenseNumber);
                    driver.setDrivingExperience(drivingExperience);

                    new DriverRegisterTask(RegisterActivity.this).execute(new Pair<Activity, Driver>(RegisterActivity.this, driver));
                }
            }
        });
    }

    public void onTandCClicked(View view){
        startActivity(new Intent(RegisterActivity.this, TandCActivity.class));
    }



    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }



    static class DriverRegisterTask extends AsyncTask<Pair<Activity, Driver>, Void, String> {
        private static DriverApi driverApiService = null;
        private Context context;
        private Driver driver;


        public DriverRegisterTask(Activity context){
            this.context = context;

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Registering...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Pair<Activity, Driver>... params) {
            if(driverApiService == null) {  // Only do this once


                DriverApi.Builder builder = new DriverApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://udriver-1312.appspot.com/_ah/api/");

/*
            UserApi.Builder builder = new UserApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("http://192.168.0.17:8080/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

                    */
                // end options for devappserver

                driverApiService = builder.build();
            }

            context = params[0].first;
            driver = params[0].second;

            try {
                if (driverApiService.insert(driver).execute().getEmail().equals(driver.getEmail()))
                    return "Successfully registered!";
                else return "Driver was NOT inserted!";
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(context, LoginActivity.class));
        }
    }





}

