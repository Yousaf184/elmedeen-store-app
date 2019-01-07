package com.example.yousafkhan.elmedeenappstore.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.example.yousafkhan.elmedeenappstore.R;
import com.example.yousafkhan.elmedeenappstore.activities.AppDetailsActivity;
import com.example.yousafkhan.elmedeenappstore.activities.HomeActivity;

// downloads the elmedeen store app update
public class AppUpdateService extends IntentService {

    public AppUpdateService() {
        super("AppUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String downloadURL = intent.getStringExtra(HomeActivity.KEY_DOWNLOAD_UPDATE);
        startAppDownload(downloadURL);
    }

    private void startAppDownload(String downloadURL) {
        Uri uri = Uri.parse(downloadURL);
        String appName = getResources().getString(R.string.app_name);

        DownloadManager.Request downloadRequest = new DownloadManager.Request(uri);
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        downloadRequest.setTitle("Updating " + appName);
        downloadRequest.setVisibleInDownloadsUi(false);
        downloadRequest.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "store_downloads/"+appName + ".apk"
        );

        DownloadManager downloadManager =
                (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        AppDetailsActivity.downloadID = downloadManager.enqueue(downloadRequest);
    }
}
