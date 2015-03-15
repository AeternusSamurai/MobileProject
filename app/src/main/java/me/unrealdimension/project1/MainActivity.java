package me.unrealdimension.project1;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;


public class MainActivity extends ActionBarActivity {
    private Chronometer mChronometer;
    long baseTime = SystemClock.elapsedRealtime();
    long stopTime;
    long elapsedTime;

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mChronometer = (Chronometer)findViewById(R.id.chronometer);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_info:
                // info item was selected
                Intent intent = new Intent(this, help.class);
                startActivity(intent);
                this.finish();
                return true;
            case R.id.menu_exit:
                // exit item was selected
                this.finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    long timeWhenStopped = 0;
    public void startTimer(View v) {
        // TODO use setBase method of the chronometer the restart it from the last time that it was started from
       // baseTime = SystemClock.elapsedRealtime() - stopTime;
        mChronometer.setBase(baseTime);
        mChronometer.start();
    }


    public void stopTimer(View v) {
        //TODO Save the time that the chronometer was stopped at so that the timer can be started from the last time it was stopped at.
        mChronometer.stop();

    }

    // TODO Get pause to work right
    public void pauseTimer(View v) {
        baseTime = mChronometer.getBase();
        mChronometer.stop();
    }
    // TODO Either write a reset method with a button or implement a long press on the stop button that calls the reset method that will reset the base of the chronometer.

}
