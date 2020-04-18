package com.sahajarora.urdriver.urdriver_driver;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class TandCActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tand_c);
    }

    public void onBackClicked(View view){
        finish();
    }
}
