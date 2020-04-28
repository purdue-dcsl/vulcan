package edu.purdue.ewok;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.support.design.widget.Snackbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.purdue.ewok.services.SensorManagerService;


/**
 * Test sensors API available on wearables; either thru the legacy service SensorManager, or
 * using the Google Fit API introduced by Google a few years back (in Google Play Services).
 */
public class MainFitnessWearActivity extends WearableActivity {

    private String TAG = "KyloSensors";

    /**
     * Bind Service
     */
    SensorManagerService mSensorManager;
    WearableActivity mActivity;
    boolean mBound = false;

    // 1: SensorManager, 2: SensorAPI (Google Fit)
    private final static int TYPE_SENSOR_API = 2;

    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // UI
    private Button btnLogin;
    private Button btnStart;
    private Button btnStop;

    // Sensor API
    private GoogleSignInClient mGoogleSignInClient;
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    private OnDataPointListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fitness_wear);
        mActivity = this;

        // UI
        init();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Enables Always-on
        setAmbientEnabled();
    }

    /**
     * Init UI
     */
    private void init() {
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger an AsyncTask that will query for a list of connected nodes
                // and send a message to each connected node
                Log.d(TAG, "Sith apprentice, identify yourself!");
                signIn();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger an AsyncTask that will query for a list of connected nodes
                // and send a message to each connected node
                Log.d(TAG, "Sith apprentice, let's do some damage!");
                startSensors();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger an AsyncTask that will query for a list of connected nodes
                // and send a message to each connected node
                Log.d(TAG, "Sith apprentice, let's stop some damage!");
                stopSensors();
            }
        });
    }

    private void startSensors() {
        if ( TYPE_SENSOR_API == 1 ) {
            mSensorManager.start();
        } else {
            findFitnessDataSourcesWrapper();
        }
    }

    private void stopSensors() {
        if ( TYPE_SENSOR_API == 1 ) {
            mSensorManager.stop();
        } else {
            //mSensorApi.stop();
            unregisterFitnessDataListener();
        }
    }

    /* ---------------------------------------------------------------------------
     * Sensor API
     *
     * References
     * Google Sign-In Android:
     * https://developers.google.com/identity/sign-in/android/start
     * --------------------------------------------------------------------------- */

    /**
     * A wrapper for {@link #findFitnessDataSourcesWrapper}. If the user account has OAuth
     * permission, continue to findFitnessDataSources, else request OAuth permission
     * for the account.
     */
    private void findFitnessDataSourcesWrapper() {
        if ( hasOAuthPermission() ) {
            Log.d(TAG, "Fitness API:: OAuth Permission Granted!");
            findFitnessDataSources();
            //mSensorApi.start(this, GoogleSignIn.getLastSignedInAccount(this));
        } else {
            Log.d(TAG, "Fitness API:: Requesting OAuth Permission");
            requestOAuthPermission();
        }
    }

    /**
     * Login using Google credentials. Starts the sign-in intent, which promps the user to sign in
     * with a Google account.
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handles signIn result.
     * @param completedTask
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    /**
     * Gets the {@link FitnessOptions} in order to check or request
     * OAuth permission for the user.
     *
     * @return the FitnessOptions
     */
    private FitnessOptions getFitnessSignInOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .build();
    }

    /**
     * Check if user's account has OAuth permission to Fitness API.
     *
     * @return true if the user has OAuth permission, false otherwise.
     */
    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
    }

    /**
     * Launches the Google SignIn activity to request OAuth permission for the user.
     */
    private void requestOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
        );
    }

    /**
     * Finds available data sources and attempts to register on a specific {@link DataType}.
     */
    private void findFitnessDataSources() {
        Log.d(TAG, "Fitness API:: Finding Fitness Data Sources...");
        Log.d(TAG, String.format("Fitness API:: Account {%s}", GoogleSignIn.getLastSignedInAccount(this)));

        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .findDataSources(
                        new DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
                                //.setDataSourceTypes(DataSource.TYPE_DERIVED)
                                //.setDataSourceTypes(DataSource.TYPE_RAW)
                                .build())
                .addOnSuccessListener(
                        new OnSuccessListener<List<DataSource>>() {
                            @Override
                            public void onSuccess(List<DataSource> dataSources) {
                                for (DataSource dataSource : dataSources) {
                                    Log.i(TAG, "Data source found: " + dataSource.toString());
                                    Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

                                    // Let's register a listener to receive Activity data!
                                    if (dataSource.getDataType().equals(DataType.TYPE_HEART_RATE_BPM)
                                            && mListener == null) {
                                        Log.i(TAG, "Data source for HEART RATE found!  Registering.");
                                        registerFitnessDataListener(dataSource, DataType.TYPE_HEART_RATE_BPM);
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "failed", e);
                            }
                        });
    }

    /**
     * Registers a listener with the Sensors API for the provided {@link DataSource} and {@link
     * DataType} combo.
     */
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        Log.d(TAG, "Fitness API:: Registering Fitness Data Listener...");
        mListener =
                new OnDataPointListener() {
                    @Override
                    public void onDataPoint(DataPoint dataPoint) {
                        for (Field field : dataPoint.getDataType().getFields()) {
                            Value val = dataPoint.getValue(field);
                            Log.i(TAG, "Detected DataPoint field: " + field.getName());
                            Log.i(TAG, "Detected DataPoint value: " + val);
                        }
                    }
                };

        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .add(
                        new SensorRequest.Builder()
                                .setDataSource(dataSource) // Optional but recommended for custom data sets.
                                .setDataType(dataType) // Can't be omitted.
                                .setSamplingRate(10, TimeUnit.SECONDS)  // sample every 10s
                                .build(),
                        mListener)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Listener registered!");
                                } else {
                                    Log.e(TAG, "Listener not registered.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Unregisters the listener with the Sensors API.
     */
    private void unregisterFitnessDataListener() {
        Log.d(TAG, "Fitness API:: Unregistering Fitness Data Listener...");
        if (mListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        Log.d(TAG, String.format("Fitness API:: Account {%s}", GoogleSignIn.getLastSignedInAccount(this)));

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.getSensorsClient(mActivity, GoogleSignIn.getLastSignedInAccount(this))
                .remove(mListener)
                .addOnCompleteListener(
                        new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if (task.isSuccessful() && task.getResult()) {
                                    Log.i(TAG, "Listener was removed!");
                                } else {
                                    Log.i(TAG, "Listener was not removed.");
                                }
                            }
                        });
        // [END unregister_data_listener]
    }

    /* ---------------------------------------------------------------------------
     * Events
     * --------------------------------------------------------------------------- */

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart ...");

        // Bind to SensorManager service
        Intent intent1 = new Intent(this, SensorManagerService.class);
        bindService(intent1, mConnectionSensorManager, Context.BIND_AUTO_CREATE);

        // Bind to Fitness service
        // Intent intent2 = new Intent(this, FitnessService.class);
        // bindService(intent2, mConnectionSensorApi, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Sith apprentice, Great Job!");

        // Destroy sensor listener
        mSensorManager.stop();
        unregisterFitnessDataListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("ActivityResult {%s}", requestCode));

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
                findFitnessDataSources();
            } else if (requestCode == RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                findFitnessDataSources();
            } else {
                // Permission denied.

                // In this Activity we've chosen to notify the user that they
                // have rejected a core permission for the app since it makes the Activity useless.
                // We're communicating this message in a Snackbar since this is a sample app, but
                // core permissions would typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.main_activity_view),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(
                                R.string.settings,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Build intent that displays the App settings screen.
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                        intent.setData(uri);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                })
                        .show();
            }
        }

    }

    /* ---------------------------------------------------------------------------
     * Service Connection
     * --------------------------------------------------------------------------- */
    private ServiceConnection mConnectionSensorManager = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            //
            SensorManagerService.LocalBinder binder = (SensorManagerService.LocalBinder) service;
            mSensorManager = binder.getService();
            mBound = true;
            Log.d(TAG, "SensorManagerService bounded!");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

}
