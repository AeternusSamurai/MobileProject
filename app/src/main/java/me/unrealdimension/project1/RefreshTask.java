package me.unrealdimension.project1;

import android.os.AsyncTask;

import android.view.View;
import android.widget.TextView;

/**
 * Created by Michelle on 4/8/2015.
 */
public class RefreshTask extends AsyncTask {
    public TextView text2;
    boolean someCondition = true;

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);

        String text = String.valueOf(System.currentTimeMillis());

        text2.setText(text);

    }

    @Override
    protected Object doInBackground(Object... params) {
        while (someCondition) {
            try {
                //sleep for 1s in background...
                Thread.sleep(1000);
                //and update textview in ui thread
                publishProgress();
            } catch (InterruptedException e) {
                e.printStackTrace();

            };
            return null;

        }
     return true;
    }

 }

