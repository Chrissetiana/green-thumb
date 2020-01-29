package com.chrissetiana.greenthumb;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chrissetiana.greenthumb.data.DbContract.PlantEntry;
import com.chrissetiana.greenthumb.data.Preferences;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link MainActivity} displays a list of plants to buy.
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private static final int PLANT_LOADER = 1;
    private static final String PLANT_DESCRIPTIONS_KEY = "plant_descriptions";
    private static final String DEFAULT_PLANT_DESCRIPTIONS_LEVEL = "basic";
    PlantAdapter mAdapter;
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    private int mRatingChoice = -1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Pass in null cursor; Cursor with plant data filled in loader's onLoadFinished
        mAdapter = new PlantAdapter(null);
        recyclerView.setAdapter(mAdapter);

        // Kick off the loader
        getLoaderManager().initLoader(PLANT_LOADER, null, this);

        // Call the line below to cause a crash
        // Any crashes will be reported to Firebase with Crash Reporting
        reportFatalError();

        // Call the line below to report a non-fatal crash
        reportNonFatalError();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(PLANT_DESCRIPTIONS_KEY, DEFAULT_PLANT_DESCRIPTIONS_LEVEL);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        fetchConfig();

        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        handleDynamicLink();
    }

    private void reportFatalError() {
//        throw new NullPointerException();
//        throw new RuntimeException("Reporting Firebase FATAL error");
//        FirebaseCrash.report(new Exception("Reporting Firebase FATAL error"));
        Log.e(TAG, "Reporting Firebase FATAL error");
    }

    private void reportNonFatalError() {
//        FirebaseCrash.report(new Exception("Reporting Firebase NON-FATAL error"));
        Log.e(TAG, "Reporting Firebase NON-FATAL error");
    }

    private void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds

        // If developer mode is enabled, reduce cache to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available
                        // via FirebaseRemoteConfig get<type> calls, e.g., getLong, getString.
                        mFirebaseRemoteConfig.activateFetched();

                        // Update the plant descriptions based on the retrieved value
                        // for plant_descriptions
                        applyRetrievedPlantDescriptionsLevel();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // An error occurred when fetching the config.
                        // Uodate the plant descriptions based on the retrieved value
                        // for plant_descriptions
                        applyRetrievedPlantDescriptionsLevel();
                    }
                });
    }

    private void applyRetrievedPlantDescriptionsLevel() {
        String plantDescriptionsLevel = mFirebaseRemoteConfig.getString(PLANT_DESCRIPTIONS_KEY);
        Log.d(TAG, "plant_descriptions = " + plantDescriptionsLevel);

        String[] plantDescriptions;

        if (plantDescriptionsLevel.equals(DEFAULT_PLANT_DESCRIPTIONS_LEVEL)) {
            plantDescriptions = getResources().getStringArray(R.array.plant_descriptions);
        } else {
            plantDescriptions = getResources().getStringArray(R.array.plant_descriptions_advanced);
        }

        for (int i = 0; i < plantDescriptions.length; i++) {
            int plantId = i + 1;
            ContentValues values = new ContentValues();
            values.put(PlantEntry.COLUMN_DESCRIPTION, plantDescriptions[i]);
            getContentResolver().update(
                    PlantEntry.CONTENT_URI,
                    values,
                    PlantEntry._ID + " = ?",
                    new String[]{String.valueOf(plantId)}
            );
        }
    }

    private void handleDynamicLink() {
        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found
        boolean autoLaunchDeepLink = false;

        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
                    @Override
                    public void onResult(@NonNull AppInviteInvitationResult result) {
                        if (result.getStatus().isSuccess()) {
                            // Extract deep link from Intent
                            Intent intent = result.getInvitationIntent();
                            String deepLink = AppInviteReferral.getDeepLink(intent);

                            // Handle the deep link. For example, open the linked
                            // content, or apply promotional credit to the user's account.
                            Uri uri = Uri.parse(deepLink);
                            int plantId = Integer.parseInt(uri.getLastPathSegment());
                            PlantDetailActivity.startActivity(MainActivity.this, plantId);
                        } else {
                            Log.d(TAG, "getInvitationL no deep link found");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Show the gardening experience rating when the app is first opened
        if (Preferences.getFirstLoad(this)) {
            showExperienceDialog();
            Preferences.setFirstLoad(this, false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_shopping_cart:
                ShoppingCartActivity.startActivity(this);
                break;
            case R.id.menu_purchases:
                PurchaseActivity.startActivity(this);
                break;
            case R.id.menu_about:
                AboutActivity.startActivity(this);
                break;
            case R.id.menu_experience:
                showExperienceDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a dialog for the user to rate their gardening experience.
     */
    private void showExperienceDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.gardening_experience_title)
                .setSingleChoiceItems(
                        R.array.gardening_experience_rating_labels,
                        Preferences.getGardeningExperience(this),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mRatingChoice = which;
                            }
                        })
                .setPositiveButton(R.string.button_gardening_experience_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mRatingChoice == -1) {
                            return;
                        }
                        Preferences.setGardeningExperience(MainActivity.this, mRatingChoice);

                        Analytics.setUserPropertyGardeningExperience(MainActivity.this, mRatingChoice);
                    }
                })
                .setNegativeButton(R.string.button_gardening_experience_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PlantEntry._ID,
                PlantEntry.COLUMN_NAME,
                PlantEntry.COLUMN_DESCRIPTION,
                PlantEntry.COLUMN_PRICE
        };
        return new CursorLoader(this,
                PlantEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "GoogleAPIClient connection failed: " + connectionResult.getErrorMessage());
    }
}
