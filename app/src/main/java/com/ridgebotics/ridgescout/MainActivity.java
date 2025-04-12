package com.ridgebotics.ridgescout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ridgebotics.ridgescout.databinding.ActivityMainBinding;
import com.ridgebotics.ridgescout.scoutingData.Fields;
import com.ridgebotics.ridgescout.utility.SentimentAnalysis;
import com.ridgebotics.ridgescout.utility.AlertManager;
import com.ridgebotics.ridgescout.utility.FileEditor;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.ridgebotics.ridgescout.utility.SettingsManager;

import java.util.Objects;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private BottomNavigationView navView;

    private AppBarConfiguration appBarConfiguration;
    private NavController navController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        SettingsManager.prefs = this.getSharedPreferences(
                "com.ridgebotics.ridgescout", Context.MODE_PRIVATE);

        if(!FileEditor.fileExist(Fields.matchFieldsFilename)){
            Fields.save(Fields.matchFieldsFilename, Fields.default_match_fields);
        }

        if(!FileEditor.fileExist(Fields.pitsFieldsFilename)){
            Fields.save(Fields.pitsFieldsFilename, Fields.default_pit_fields);
        }


        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        AlertManager.init(this);
        SentimentAnalysis.init(this);

        Objects.requireNonNull(getSupportActionBar()).hide();




        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());





        navView = findViewById(R.id.nav_view);

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_scouting,
                R.id.navigation_data_parent,
                R.id.navigation_transfer,
                R.id.navigation_settings)
                .build();

//        appBarConfiguration.set

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);


        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navView.setOnItemSelectedListener(item -> {
            backPressed = null;
            clearBackStack();
            navController.navigate(item.getItemId(), savedInstanceState, new NavOptions.Builder()
                    .setEnterAnim(R.anim.enter_anim)
                    .setExitAnim(R.anim.exit_anim)
                    .setPopEnterAnim(R.anim.pop_enter_anim)
                    .setPopExitAnim(R.anim.pop_exit_anim).build()
            );
            return true;
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


    public interface onBackPressed {
        boolean onBackPressed();
    }

    public onBackPressed backPressed = null;
    public void setOnBackPressed(onBackPressed onBackPressed){
        this.backPressed = onBackPressed;
    }



    @Override
    public void onBackPressed() {
        if(backPressed != null) {
            if (backPressed.onBackPressed()) {
                super.onBackPressed();
            }
        } else {super.onBackPressed();}
    }

}