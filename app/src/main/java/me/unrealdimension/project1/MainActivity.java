package me.unrealdimension.project1;


/*
Importing Android Class And Google FIT API
 */
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "FitActivity";
    //[START Auth_Variable_References]
    private static final int REQUEST_OAUTH = 1;
    // [END auth_variable_references]
    private GoogleApiClient mClient = null;

    int mInitialNumberOfSteps = 0;
    private TextView mStepsTextView;
    private boolean mFirstCount = true;
    private TextView countDown;
    // Create Builder View
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStepsTextView = (TextView) findViewById(R.id.step_counter);

        countDown = (TextView) findViewById(R.id.coutdDownTimer);
        //  new RefreshTask().execute();
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                countDown.setText("Warm Up Arms for: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                countDown.setText("Good job!");
            }

        }.start();



    }

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

    private void connectFitness() {
        Log.i(TAG, "Connecting...");

        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                // select the Fitness API
                .addApi(Fitness.SENSORS_API)    // Required only if you're making SensorsApi calls
                .addApi(Fitness.RECORDING_API)  // Required only if you're making RecordingApi calls
                .addApi(Fitness.HISTORY_API)    // Required only if you're making HistoryApi calls
                .addApi(Fitness.SESSIONS_API)
                        // specify the scopes of access
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                        // provide callbacks
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Connect the Google API client
        mClient.connect();
    }

    // Manage OAuth authentication
    @Override
    public void onConnectionFailed(ConnectionResult result) {

        // Error while connecting. Try to resolve using the pending intent returned.
        if (result.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED ||
                result.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
            try {
                // Request authentication
                result.startResolutionForResult(this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception connecting to the fitness service", e);
            }
        } else {
            Log.e(TAG, "Unknown connection issue. Code = " + result.getErrorCode());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == RESULT_OK) {
                // If the user authenticated, try to connect again
                mClient.connect();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // If your connection gets lost at some point,
        // you'll be able to determine the reason and react to it here.
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "Connected!");

        // Now you can make calls to the Fitness APIs.
        invokeFitnessAPIs();

    }

    private void invokeFitnessAPIs() {

        // Create a listener object to be called when new data is available
        OnDataPointListener listener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {

                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    updateTextViewWithStepCounter(val.asInt());
                }
            }
        };

        //Specify what data sources to return
        DataSourcesRequest req = new DataSourcesRequest.Builder()
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                .build();

        //  Invoke the Sensors API with:
        // - The Google API client object
        // - The data sources request object
        PendingResult<DataSourcesResult> pendingResult =
                Fitness.SensorsApi.findDataSources(mClient, req);

        //  Build a sensor registration request object
        SensorRequest sensorRequest = new SensorRequest.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build();

        //  Invoke the Sensors API with:
        // - The Google API client object
        // - The sensor registration request object
        // - The listener object
        PendingResult<Status> regResult =
                Fitness.SensorsApi.add(mClient,
                        new SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .build(),
                        listener);


        // 4. Check the result asynchronously
        regResult.setResultCallback(new ResultCallback<Status>()
        {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "listener registered");
                    // listener registered
                } else {
                    Log.d(TAG, "listener not registered");
                    // listener not registered
                }
            }
        });
    }

    // Update the Text Viewer with Counter of Steps..
    private void updateTextViewWithStepCounter(final int numberOfSteps) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "On Datapoint!", Toast.LENGTH_SHORT);

                if(mFirstCount && (numberOfSteps != 0)) {
                    mInitialNumberOfSteps = numberOfSteps;
                    mFirstCount = false;
                }
                if(mStepsTextView != null){
                    mStepsTextView.setText(String.valueOf(numberOfSteps - mInitialNumberOfSteps));
                }
            }
        });
    }

    //Start
    @Override
    protected void onStart() {
        super.onStart();
        mFirstCount = true;
        mInitialNumberOfSteps = 0;
        if (mClient == null || !mClient.isConnected()) {
            connectFitness();
        }
    }
    //Stop
    @Override
    protected void onStop() {
        super.onStop();
        if(mClient.isConnected() || mClient.isConnecting()) mClient.disconnect();
        mInitialNumberOfSteps = 0;
        mFirstCount = true;
    }

}