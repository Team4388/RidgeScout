package com.ridgebotics.ridgescout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.ridgebotics.ridgescout.databinding.ActivityMainBinding;
import com.ridgebotics.ridgescout.scoutingData.fields;
import com.ridgebotics.ridgescout.utility.SentimentAnalysis;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.fileEditor;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.ridgebotics.ridgescout.utility.settingsManager;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private BottomNavigationView navView;

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        settingsManager.prefs = this.getSharedPreferences(
                "com.ridgebotics.ridgescout", Context.MODE_PRIVATE);

        if(!fileEditor.fileExist(fields.matchFieldsFilename)){
            fields.save(fields.matchFieldsFilename, fields.default_match_fields);
        }

        if(!fileEditor.fileExist(fields.pitsFieldsFilename)){
            fields.save(fields.pitsFieldsFilename, fields.default_pit_fields);
        }

        AlertManager.init(this);
        SentimentAnalysis.init(this);

        Objects.requireNonNull(getSupportActionBar()).hide();




        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());





        navView = findViewById(R.id.nav_view);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_scouting,
                R.id.navigation_data,
                R.id.navigation_transfer,
                R.id.navigation_settings)
                .build();

//        appBarConfiguration.set

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);


        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                clearBackStack();
                navController.navigate(item.getItemId(), savedInstanceState, new NavOptions.Builder()
                        .setEnterAnim(R.anim.enter_anim)
                        .setExitAnim(R.anim.exit_anim)
                        .setPopEnterAnim(R.anim.pop_enter_anim)
                        .setPopExitAnim(R.anim.pop_exit_anim).build()
                );
                return true;
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void clearBackStack() {
        navController.popBackStack(navController.getGraph().getStartDestinationId(), false);
    }




    public interface activityResultRelay {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public static activityResultRelay resultRelay = null;
    public static void setResultRelay(activityResultRelay tmpresultRelay){
        resultRelay = tmpresultRelay;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        AlertManager.error(String.valueOf(requestCode));
        if (resultRelay != null) {
            resultRelay.onActivityResult(resultCode, requestCode, data);
        }
    }

}