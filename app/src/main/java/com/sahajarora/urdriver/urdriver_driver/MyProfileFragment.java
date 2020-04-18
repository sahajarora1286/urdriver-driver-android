package com.sahajarora.urdriver.urdriver_driver;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sahajarora.urdriver.urdriver_driver.helper.SQLiteHandler;
import com.sahajarora.urdriver.urdriver_driver.helper.SessionManager;

import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class MyProfileFragment extends Fragment {
    private Button btnParty, btnAirport, btnValet, btnCancelBooking;
    private SQLiteHandler db;
    private SessionManager session;
    private View view;
    private TextView tvName, tvAddress, tvPostalCode, tvEmail, tvPhone, tvLicenseNumber;

    public MyProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        // SqLite database handler
        db = new SQLiteHandler(getActivity().getApplicationContext());

        // session manager
        session = new SessionManager(getActivity().getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }


        tvName = (TextView) view.findViewById(R.id.tvName);
        tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        tvPostalCode = (TextView) view.findViewById(R.id.tvPostalCode);
        tvEmail = (TextView) view.findViewById(R.id.tvEmail);
        tvPhone = (TextView) view.findViewById(R.id.tvPhone);
        tvLicenseNumber = (TextView) view.findViewById(R.id.tvLicenseNumber);

        HashMap<String, String> user = new HashMap<>();

        user = db.getDriverDetails();

        tvName.setText(user.get("name"));
        tvAddress.setText(user.get("address"));
        tvPostalCode.setText(user.get("postalCode"));
        tvEmail.setText(user.get("email"));
        tvPhone.setText(user.get("phone"));
        tvLicenseNumber.setText(user.get("licenseNumber"));

        return view;
    }

    public void logoutUser() {


        session.setLogin(false);

        db.deleteUsers();
        Toast.makeText(getActivity().getApplicationContext(), "You have been logged out!", Toast.LENGTH_LONG).show();
        // Launching the login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }


}
