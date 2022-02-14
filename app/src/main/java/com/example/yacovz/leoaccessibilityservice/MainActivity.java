package com.example.yacovz.leoaccessibilityservice;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        button.setOnClickListener( mWazeListener );





       /* String uri = "waze://?ll=32.141598,34.801754&navigate=yes";
        //uri = String.format(Locale.ENGLISH,"geo:32.0728374,34.7931619" ); // 32.141598, 34.801754
        Intent intent= new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*String uri = "waze://?ll=32.141598,34.801754&navigate=yes";
        //uri = String.format(Locale.ENGLISH,"geo:32.0728374,34.7931619" ); // 32.141598, 34.801754
        Intent intent= new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }

    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener mWazeListener = new View.OnClickListener() {
        public void onClick(View v) {

            EditText edit = findViewById(R.id.editText);

            setGroupName(edit.getText().toString());

            Log.v("JAccessibilityService", "Save Response: " + getGroupName ());

           /* // do something when the button is clicked
            // Yes we will handle click here but which button clicked??? We don't know
            String uri = "waze://?ll=32.141598,34.801754&navigate=yes";
            //uri = String.format(Locale.ENGLISH,"geo:32.0728374,34.7931619" ); // 32.141598, 34.801754
            Intent intent= new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/
        }
    };

    private String groupNamePrefs = "group_name_prefs";
    private String groupNameKey = "group_name_key";

    private String getGroupName ()
    {
        SharedPreferences sp = getSharedPreferences(groupNamePrefs, Activity.MODE_PRIVATE);
        return sp.getString(groupNameKey, "");
    }

    private void setGroupName (String groupName)
    {
        if (!groupName.isEmpty()){
            SharedPreferences sp = getSharedPreferences(groupNamePrefs, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(groupNameKey, groupName );
            editor.commit();
        }
    }
}
