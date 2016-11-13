package org.atthack.november16;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setupSpinners();
    }

    private void setupSpinners() {
        final Spinner language = (Spinner) findViewById(R.id.ddLanguage);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language, R.layout.spinner_center);

// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        language.setAdapter(adapter);

        final Spinner city = (Spinner) findViewById(R.id.ddCity);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterCity = ArrayAdapter.createFromResource(this,
                R.array.city, R.layout.spinner_center);
// Specify the layout to use when the list of choices appears
        adapterCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        city.setAdapter(adapterCity);



        Button btnExplore = (Button)findViewById(R.id.btnExplore);
        btnExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MapsActivity.class);
                String cityJSON;
                String lang;
                if (city.getSelectedItemId() == 1) {
                    cityJSON = "madison-es.json";
                    lang = "spa-MEX";
                } else {
                    cityJSON = "atlanta.json";
                    lang = "en-USA";
                }
                intent.putExtra("city", cityJSON);
                intent.putExtra("lang", lang);
                startActivity(intent);



            }
        });

    }

}
