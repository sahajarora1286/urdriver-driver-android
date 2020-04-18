package com.sahajarora.urdriver.urdriver_driver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.sahajarora.urdriver.urdriver_driver.helper.SQLiteHandler;
import com.sahajarora.urdriver.urdriver_driver.helper.SessionManager;
import com.urdriver.sahajarora.myapplication.backend.driverApi.DriverApi;
import com.urdriver.sahajarora.myapplication.backend.driverApi.model.Driver;

import java.io.IOException;

public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button btnSignIn, btnRegister;
    private EditText etUsername, etPassword;
    private static ProgressDialog pDialog;
    private static SessionManager session;
    private static SQLiteHandler db;
    public static Driver currentDriver = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!username.isEmpty() && !password.isEmpty()) {
                    // login user
                    //checkLogin(username, password);
                    new LoadUserAsyncTask(LoginActivity.this).execute(new Pair<String, String>(username, password));

                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });

    }

    static class LoadUserAsyncTask extends AsyncTask<Pair<String, String>, Void, Driver> {
        private static DriverApi driverApiService = null;

        private Activity activity;
        private Driver driver;



        public LoadUserAsyncTask(Activity activity){
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Signing in...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Driver doInBackground(Pair<String, String>... params) {
            if (driverApiService == null) {  // Only do this once


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

            String email = params[0].first;
            String password = params[0].second;

            try {
                return driverApiService.getDriverByEmailPassword(email, password).execute();


            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Driver result) {

            //
            if (result != null) {
                currentDriver = result;

                db.addDriver(currentDriver.getName(), currentDriver.getEmail(), currentDriver.getAddress(), currentDriver.getPostalCode(),
                        currentDriver.getPhone(), currentDriver.getId(), currentDriver.getLicenseNumber(), currentDriver.getDrivingExperience());
                session.setLogin(true);
                pDialog.dismiss();
                activity.startActivity(new Intent(activity, HomeActivity.class));

            }
            else {
                pDialog.dismiss();
                Toast.makeText(activity.getApplicationContext(), "Incorrect Credentials!", Toast.LENGTH_LONG).show();

            }
        }
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
