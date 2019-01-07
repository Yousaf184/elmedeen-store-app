package com.example.yousafkhan.elmedeenappstore.activities;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appaholics.updatechecker.UpdateChecker;
import com.example.yousafkhan.elmedeenappstore.BroadcastReceivers.DownloadCompleteReceiver;
import com.example.yousafkhan.elmedeenappstore.Interfaces.RecyclerViewItemClick;
import com.example.yousafkhan.elmedeenappstore.Models.App;
import com.example.yousafkhan.elmedeenappstore.R;
import com.example.yousafkhan.elmedeenappstore.Utils.MyUtils;
import com.example.yousafkhan.elmedeenappstore.recyclerview.RcAdapter;
import com.example.yousafkhan.elmedeenappstore.services.AppUpdateService;
import com.example.yousafkhan.elmedeenappstore.view_models.HomeActivityVM;

import java.io.File;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements RecyclerViewItemClick {

    private ProgressBar progressBar;
    private TextView infoText;
    private Button retryButton;

    public static final String INTENT_EXTRA_KEY = "item_position";
    public static final String SHARED_IMAGE_TRANSITION_NAME = "sharedImageTransitionName";

    private static final String STORE_UPDATE_VERSION_FILE_URL =
            "http://www.elmedeen.com/mobile/elmedeen_app_store/storeupdate/store_version.txt";

    private static final String STORE_UPDATE_APK_FILE_URL =
            "http://www.elmedeen.com/mobile/elmedeen_app_store/storeupdate/elmedeen_store.apk";

    public static final String KEY_DOWNLOAD_UPDATE = "downloadAppUpdate";

    private HomeActivityVM viewmodel;

    private BroadcastReceiver downloadCompleteReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        progressBar = findViewById(R.id.progress_bar_home);
        infoText = findViewById(R.id.info_text);
        retryButton = findViewById(R.id.retry_button);
        viewmodel = ViewModelProviders.of(this).get(HomeActivityVM.class);

        // set toolbar as action bar
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        if(MyUtils.isInternetConnected(this)) {
            setViewmodelAndObservers();
        } else {
            progressBar.setVisibility(View.GONE);
            makeSnackBar("Internet not connected");
        }

        checkForStoreUpdate();

        // delete updated apk file if it exists from the downloads/store_downloads directory
        deleteApkFileIfExists();
    }

    private void deleteApkFileIfExists() {
        String appName = getResources().getString(R.string.app_name);

        File downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File directory = new File(
                downloadsDirectory+"/store_downloads/" + appName + ".apk"
        );

        if(directory.exists()) {
            directory.delete();
        }
    }

    private void makeSnackBar(String message) {
        CoordinatorLayout coordinatorLayout = findViewById(R.id.home_activity_parent_layout);
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .show();
    }

    private void checkForStoreUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateChecker checker = new UpdateChecker(HomeActivity.this, false);
                checker.checkForUpdateByVersionCode(STORE_UPDATE_VERSION_FILE_URL);

                if(checker.isUpdateAvailable()) {
                    showUpdateConfirmationDialog();
                }
            }
        }).start();
    }

    private void showUpdateConfirmationDialog() {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        alertBuilder.setTitle("Update Available");
        alertBuilder.setMessage("Do you want to download and install update right now");
        alertBuilder.setIcon(R.drawable.ic_update);

        alertBuilder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(MyUtils.isInternetConnected(HomeActivity.this)) {

                    if(MyUtils.hasExternalStorageWritePermission(HomeActivity.this)) {
                        downloadStoreUpdate();
                    } else {
                        ActivityCompat.requestPermissions(
                                HomeActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                AppDetailsActivity.REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE
                        );
                    }

                } else {
                    makeSnackBar("Could not download update, check your internet connection");
                }
            }
        });

        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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

    private void downloadStoreUpdate() {
        downloadCompleteReceiver = new DownloadCompleteReceiver(this);
        registerReceiver(
                downloadCompleteReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        );

        Intent downloadAppUpdate = new Intent(HomeActivity.this, AppUpdateService.class);
        downloadAppUpdate.putExtra(KEY_DOWNLOAD_UPDATE, STORE_UPDATE_APK_FILE_URL);
        startService(downloadAppUpdate);
    }

    private void setViewmodelAndObservers() {

        viewmodel.getDataList().observe(this, new Observer<List<App>>() {
            @Override
            public void onChanged(@Nullable List<App> appList) {
                progressBar.setVisibility(View.GONE);

                if(appList.size() > 0) {
                    displayAppList(appList);
                } else {
                    infoText.setText("App store is currently empty");
                    infoText.setVisibility(View.VISIBLE);
                }
            }
        });

        viewmodel.getServer_error_message().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                progressBar.setVisibility(View.GONE);
                infoText.setText(s);
                infoText.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayAppList(List<App> dataList) {
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, calculateNoOfColumns());
        recyclerView.setLayoutManager(gridLayoutManager);

        RcAdapter adapter = new RcAdapter(dataList, this);
        recyclerView.setAdapter(adapter);
    }

    // called when any item in RecyclerView is clicked
    // called from RcAdapter.RcViewHolder class
    @Override
    public void itemClicked(int position, ImageView appTitleImage) {
        Intent openAppDetailActivity = new Intent(this, AppDetailsActivity.class);
        openAppDetailActivity.putExtra(INTENT_EXTRA_KEY, position);

        // for shared element transition via RecyclerView row on app title ImageView
        openAppDetailActivity.putExtra(
                SHARED_IMAGE_TRANSITION_NAME,
                ViewCompat.getTransitionName(appTitleImage)
        );

        // shared element transition
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                this,
                appTitleImage,
                ViewCompat.getTransitionName(appTitleImage)
        );

        startActivity(openAppDetailActivity, activityOptions.toBundle());
    }

    public void tryLoadingDataAgain(View view) {
        progressBar.setVisibility(View.VISIBLE);
        infoText.setVisibility(View.INVISIBLE);
        retryButton.setVisibility(View.GONE);
        viewmodel.loadAllApps();
    }

    // calculates the number of grid columns in recycler view according to screen width
    public int calculateNoOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 190); // 190 is the width of the grid item
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {
            case AppDetailsActivity.REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadStoreUpdate();
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

    private void showPermissionRequestExplanation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(HomeActivity.this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(downloadCompleteReceiver != null) {
            unregisterReceiver(downloadCompleteReceiver);
        }
    }
}
