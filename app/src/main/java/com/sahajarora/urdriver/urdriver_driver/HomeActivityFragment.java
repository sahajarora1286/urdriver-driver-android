package com.sahajarora.urdriver.urdriver_driver;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sahajarora.urdriver.urdriver_driver.helper.SQLiteHandler;
import com.sahajarora.urdriver.urdriver_driver.helper.SessionManager;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends Fragment {
    private Button btnBookings, btnAvailability, btnComments;
    private SQLiteHandler db;
    private SessionManager session;
    public static String driverType;
    private View view;

    public HomeActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_home_activity_fragment, container, false);

        // SqLite database handler
        db = new SQLiteHandler(getActivity().getApplicationContext());

        // session manager
        session = new SessionManager(getActivity().getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        btnBookings = (Button) view.findViewById(R.id.btnBookings);
        btnAvailability = (Button) view.findViewById(R.id.btnAvailability);
        btnComments = (Button) view.findViewById(R.id.btnComments);


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

