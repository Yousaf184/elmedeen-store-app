package com.example.yousafkhan.elmedeenappstore.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.yousafkhan.elmedeenappstore.BroadcastReceivers.AllDownloadRequestsCompleteReceiver;
import com.example.yousafkhan.elmedeenappstore.BroadcastReceivers.DownloadCancelledReceiver;
import com.example.yousafkhan.elmedeenappstore.BroadcastReceivers.DownloadCompleteReceiver;
import com.example.yousafkhan.elmedeenappstore.BroadcastReceivers.DownloadProgressReceiver;
import com.example.yousafkhan.elmedeenappstore.Models.App;
import com.example.yousafkhan.elmedeenappstore.R;
import com.example.yousafkhan.elmedeenappstore.Utils.MyUtils;
import com.example.yousafkhan.elmedeenappstore.services.MyDownloadService;
import com.example.yousafkhan.elmedeenappstore.view_models.HomeActivityVM;

import java.io.File;
import java.util.List;
import java.util.Locale;

import br.com.felix.imagezoom.ImageZoom;

public class AppDetailsActivity extends AppCompatActivity {

    private static final int UNINSTALL_REQUEST_CODE = 2;
    public static final int REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE = 1;

    public static final String KEY_DOWNLOAD_URL = "downloadUrlKey";

    //private static final String tag = "yousafdebug";

    // holds a reference to the app whose details are to be displayed
    public static App currentApp;

    public static long downloadID = -1;

    private int shortAnimationDuration;
    private int mediumAnimationDuration;

    private ImageView appTitleImage;
    private TextView appName;
    private TextView appDescription;
    private ImageZoom appScreenShot1;
    private ImageZoom appScreenShot2;
    private ProgressBar downloadProgressBar;
    private Button openAppBtn;
    private Button installAppBtn;
    private TextView downloadedBytes;
    private TextView downloadedPercent;

    private BroadcastReceiver downloadCompleteReceiver;
    private BroadcastReceiver downloadProgressReceiver;
    private BroadcastReceiver allDownloadsCompletedReceiver;
    private BroadcastReceiver downloadCancelledReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        // delay shared element transition
        // so that AppDetailActivity has time to load the app title image
        postponeEnterTransition();

        appTitleImage = findViewById(R.id.app_title_image_DACT);
        appName = findViewById(R.id.app_name_text_DACT);
        appDescription = findViewById(R.id.app_description);
        appScreenShot1 = findViewById(R.id.app_screenshot1);
        appScreenShot2 = findViewById(R.id.app_screenshot2);
        downloadProgressBar = findViewById(R.id.download_progress_bar);
        openAppBtn = findViewById(R.id.openApp_btn);
        installAppBtn = findViewById(R.id.install_btn);
        downloadedBytes = findViewById(R.id.downloaded_bytes_text);
        downloadedPercent = findViewById(R.id.downloaded_percent_text);

        // used for cross fade animation between download progress bar and buttons
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        downloadProgressReceiver = new DownloadProgressReceiver(this);
        registerReceiver(
                downloadProgressReceiver,
                new IntentFilter(MyDownloadService.BROADCAST_ACTION_DOWNLOAD_SERVICE)
        );

        downloadCancelledReceiver = new DownloadCancelledReceiver(this);
        registerReceiver(
                downloadCancelledReceiver,
                new IntentFilter(MyDownloadService.BROADCAST_ACTION_DOWNLOAD_CANCELLED)
        );

        // set toolbar as action bar
        Toolbar toolbar = findViewById(R.id.appDetails_toolbar);
        setSupportActionBar(toolbar);

        // set back arrow icon on action bar
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        // set shared element transition name on app title ImageView
        // transition name is set on runtime because of ReccylerView
        String sharedImgTransitionName = getIntent().getStringExtra(HomeActivity.SHARED_IMAGE_TRANSITION_NAME);
        appTitleImage.setTransitionName(sharedImgTransitionName);

        // fade in all the views except app title image
        animateFadeIn(appName, 1000);
        animateFadeIn(appScreenShot1, 1000);
        animateFadeIn(appScreenShot2, 1000);
        animateFadeIn(installAppBtn, 1000);
        animateFadeIn(appDescription, 1000);

        // get intent extra
        // it contains the position of the view which is clicked in RecyclerView
        int position = getIntent().getIntExtra(HomeActivity.INTENT_EXTRA_KEY, -1);
        List<App> appList = HomeActivityVM.dataList.getValue();

        if(appList != null) {
            currentApp = appList.get(position);

            // if app is already installed, display open app button
            if(isAppAlreadyInstalled(currentApp.getAppPackageName())) {
                animateFadeIn(openAppBtn, 1000);
                installAppBtn.setText("Uninstall");
            }

            displayAppDetails(currentApp);
        }

        changeInstallButtonStatus();
    }

    private void displayAppDetails(App app) {
        Glide.with(this)
                .load(app.getAppTitleImageURL())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {

                        // image successfully loaded, start postponed transition
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(appTitleImage);

        appName.setText(app.getAppNameEng());
        appDescription.setText(app.getAppDescriptionEng());

        Glide.with(this)
                .load(app.getAppScreenshot1URL())
                .into(appScreenShot1);

        Glide.with(this)
                .load(app.getAppScreenshot2URL())
                .into(appScreenShot2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void openApp(View v) {
        PackageManager pm = getPackageManager();
        Intent openInstalledApp = pm.getLaunchIntentForPackage(currentApp.getAppPackageName());
        startActivity(openInstalledApp);
    }

    public void installOrUninstallApp(View v) {
        if(isAppAlreadyInstalled(currentApp.getAppPackageName())) {
            uninstallAPP(currentApp.getAppPackageName());
        } else {
            downloadAndInstallApp();
        }
    }

    private void downloadAndInstallApp() {
        // check for internet connection
        if(MyUtils.isInternetConnected(this)) {

            // check if external storage is available
            if(isExternalStorageWritable()) {

                // check android version, must be greater of equal to Marshmallow
                // for runtime permission request
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if(hasExternalStorageWritePermission()) {
                        animateFadeOut(installAppBtn, shortAnimationDuration);
                        animateFadeIn(downloadProgressBar, mediumAnimationDuration);
                        animateFadeIn(downloadedBytes, mediumAnimationDuration);
                        animateFadeIn(downloadedPercent, mediumAnimationDuration);
                        startAppDownload();
                    } else {
                        // ask for permission
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE
                        );
                    }
                }
            }

        } else {
            CoordinatorLayout coordinatorLayout = findViewById(R.id.home_activity_parent_layout);
            Snackbar.make(coordinatorLayout, "Internet not connected", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void startAppDownload() {
        Log.d("tag", "starting app download");
        // broadcast receivers are registered and unregistered using application context
        // because they will be unregistered from another class, if application context
        // is not used, it will cause intent leak exception,
        allDownloadsCompletedReceiver = new AllDownloadRequestsCompleteReceiver(this);
        getApplicationContext().registerReceiver(
                allDownloadsCompletedReceiver,
                new IntentFilter(MyDownloadService.BROADCAST_ACTION_ALL_DOWNLOADS_COMPLETED)
        );

        downloadCompleteReceiver = new DownloadCompleteReceiver(this);
        getApplicationContext().registerReceiver(
                downloadCompleteReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        );

        String url = currentApp.getAppDownloadURL();
        Intent startDownloadService = new Intent(this, MyDownloadService.class);
        startDownloadService.putExtra(KEY_DOWNLOAD_URL, url);
        startService(startDownloadService);
    }

    public void displayDownloadProgress(final String bytesDownloaded,
                                         final String totalBytes, final int percentDownloaded,
                                         final boolean setProgBarDeterminate) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(setProgBarDeterminate) {
                            downloadProgressBar.setIndeterminate(false);
                        }

                        downloadedBytes.setText(
                                String.format(
                                        Locale.ENGLISH,
                                        "%s Mb / %s Mb",
                                        bytesDownloaded, totalBytes
                                )
                        );

                        downloadedPercent.setText(
                                String.format(Locale.ENGLISH,"%d %%", percentDownloaded)
                        );

                        downloadProgressBar.setProgress(percentDownloaded);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // change the downloadID to any number other than -1
                    //
                    // needed to avoid hiding the download progress bar in changeStatus
                    // method when activity resumes after permission dialog is closed
                    downloadID = -2;

                    animateFadeOut(installAppBtn, shortAnimationDuration);
                    animateFadeIn(downloadProgressBar, mediumAnimationDuration);
                    animateFadeIn(downloadedBytes, mediumAnimationDuration);
                    animateFadeIn(downloadedPercent, mediumAnimationDuration);

                    startAppDownload();
                } else {

                    // should show permission explanation?
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        showPermissionRequestExplanation();
                    }
                }

                break;
        }
    }

    // if any other app is already downloading, disable the install button
    // so that user can only install one app at a time
    private void changeInstallButtonStatus() {
        int status = getDownloadStatus(downloadID);

        if(status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING
                || status == DownloadManager.STATUS_PAUSED) {

            installAppBtn.setEnabled(false);
            installAppBtn.setBackgroundColor(Color.GRAY);
        } else {
            installAppBtn.setEnabled(true);
            installAppBtn.setBackground(getResources().getDrawable(R.drawable.round_button_bg));
        }
    }

    private void showPermissionRequestExplanation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AppDetailsActivity.this);
                alertBuilder.setTitle("Why access to external storage is required?");
                alertBuilder.setMessage("Access to external storage is required to download and install the apps");
                alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertBuilder.create().show();
                    }
                });
            }
        }).start();
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    private boolean hasExternalStorageWritePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isAppAlreadyInstalled(String appPackageName) {
        boolean isInstalled = true;
        PackageManager pkgManager = getPackageManager();

        try {
            pkgManager.getPackageInfo(appPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            isInstalled = false;
        }

        return isInstalled;
    }

    private void uninstallAPP(String packageName) {
        Intent intent=new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE && resultCode == RESULT_OK) {
            openAppBtn.setVisibility(View.INVISIBLE);
            installAppBtn.setText("Install");
        }
    }

    private void animateFadeIn(View v, int animationTime) {
        v.setVisibility(View.VISIBLE);
        v.setAlpha(0f);

        v.animate()
                .alpha(1f)
                .setDuration(animationTime)
                .setListener(null);
    }

    private void animateFadeOut(final View v, int animationTime) {
        v.animate()
                .alpha(0f)
                .setDuration(animationTime)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.INVISIBLE);
                        v.setAlpha(1f);
                    }
                });
    }

    // called when activity is about to be destroyed or stopped
    // if download is in progress, we should know about it
    // so that when app detail activity is opened again, UI can
    // be updated to show the status of the running download
    private int getDownloadStatus (long download_id) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query downloadQuery = new DownloadManager.Query();
        downloadQuery.setFilterById(download_id);

        Cursor cursor = downloadManager.query(downloadQuery);

        if(cursor.moveToFirst()) {
            int status = cursor.getInt(
                    cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            );

            cursor.close();

            return status;
        }

        return -1;
    }

    private String getDownloadTitle (long download_id) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query downloadQuery = new DownloadManager.Query();
        downloadQuery.setFilterById(download_id);

        Cursor cursor = downloadManager.query(downloadQuery);

        if(cursor.moveToFirst()) {
            String downloadTitle = cursor.getString(
                    cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
            );

            cursor.close();

            return downloadTitle;
        }

        return "";
    }

    // save download id and current app package name in shared preferences
    // app package name is key and download id is value
    public void saveDownloadInfo(long download_id, String appPackageName) {
        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putLong(appPackageName, download_id);

        editor.apply();
    }

    // shows "install" button and hides "open" button
    private void showAppNotInstalledState() {
        installAppBtn.setText("Install");
        installAppBtn.setVisibility(View.VISIBLE);
        openAppBtn.setVisibility(View.INVISIBLE);
    }

    // shows "open" button and changes text of "install" button to "uninstall"
    private void showAppInstalledState() {
        openAppBtn.setVisibility(View.VISIBLE);
        installAppBtn.setText("Uninstall");
        installAppBtn.setVisibility(View.VISIBLE);
    }

    // hide the download progress bar and related TextViews
    private void hideDownloadProgressIndicators() {
        downloadProgressBar.setVisibility(View.GONE);
        downloadedBytes.setVisibility(View.GONE);
        downloadedPercent.setVisibility(View.GONE);
    }

    // removes the key and its value from SharedPreferences
    private void removeKeyFromSharedPreferences(String key) {
        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.remove(key);
        editor.apply();
    }

    private boolean sharedPreferenceHasKey(String key) {
        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        return sharedPrefs.contains(key);
    }

    // called from AllDownloadsCompleteReceiver
    public void unregisterDownloadReceivers() {
        getApplicationContext().unregisterReceiver(downloadCompleteReceiver);
        getApplicationContext().unregisterReceiver(allDownloadsCompletedReceiver);
    }

    private long getDataFromSharedPreference(String key) {
        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        return sharedPrefs.getLong(key, -1);
    }

    public void changeAppState() {
        if(isAppAlreadyInstalled(currentApp.getAppPackageName()) && downloadID == -1) {
            showAppInstalledState();
            hideDownloadProgressIndicators();

        } else if(!isAppAlreadyInstalled(currentApp.getAppPackageName()) && downloadID == -1) {
            showAppNotInstalledState();
            hideDownloadProgressIndicators();
        }
    }

    // check if any apk file with the same name as the current app
    // exists in the /downloads/store_downloads directory

    // if it exists, delete that file
    private void deleteApkFileIfExists() {
        File downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File directory = new File(
           downloadsDirectory+"/store_downloads/" + currentApp.getAppNameEng() + ".apk"
        );

        if(directory.exists()) {
            directory.delete();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // remove leftover .apk file with the same name as
        // currentApp's name in the downloads directory if it exists
        if(downloadID == -1) {
            deleteApkFileIfExists();
        }

        // check is shared preference has a key with the same name as current app's
        // package name, if it does, that means there was some download in progress
        // while the detail activity stopped
        if(sharedPreferenceHasKey(currentApp.getAppPackageName())) {
            // key found, shared preference not empty
            long download_id = getDataFromSharedPreference(currentApp.getAppPackageName());

            // initialize the downloadID again from the download id retrieved from
            // shared preferences so that install prompt can be shown when download
            // completes
            downloadID = download_id;

            // check if download is still in progress
            int downloadStatus = getDownloadStatus(download_id);

            if(downloadStatus == DownloadManager.STATUS_RUNNING
                    || downloadStatus == DownloadManager.STATUS_PENDING) {

                // download still in progress
                installAppBtn.setVisibility(View.INVISIBLE);
                downloadProgressBar.setVisibility(View.VISIBLE);
                downloadedPercent.setVisibility(View.VISIBLE);
                downloadedBytes.setVisibility(View.VISIBLE);
            } else {
                removeKeyFromSharedPreferences(currentApp.getAppPackageName());
                // reset the download ID
                // needed for changeAppState method
                downloadID = -1;
                changeAppState();
            }

        } else {
            // key not found
            changeAppState();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(downloadID != -1) {
            int downloadStatus = getDownloadStatus(downloadID);
            String downloadTitle = getDownloadTitle(downloadID);

            if((downloadStatus == DownloadManager.STATUS_RUNNING
                    || downloadStatus == DownloadManager.STATUS_PENDING)
                    && currentApp.getAppNameEng().equals(downloadTitle)) {

                saveDownloadInfo(downloadID, currentApp.getAppPackageName());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(downloadProgressReceiver);
        unregisterReceiver(downloadCancelledReceiver);
    }
}
