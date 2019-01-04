package com.example.yousafkhan.elmedeenappstore.services;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.example.yousafkhan.elmedeenappstore.activities.AppDetailsActivity;

import java.text.DecimalFormat;

public class MyDownloadService extends IntentService {

    public static final String BROADCAST_ACTION_DOWNLOAD_SERVICE = "downloadServiceBroadcast";
    public static final String BROADCAST_ACTION_ALL_DOWNLOADS_COMPLETED = "allDownloadsCompleted";
    public static final String BROADCAST_ACTION_DOWNLOAD_CANCELLED = "appDownloadCancelled";

    public static final String KEY_BYTES_DOWNLOADED_MB = "bytesDownloadMB";
    public static final String KEY_PERCENT_DOWNLOADED = "percentDownloaded";
    public static final String KEY_TOTAL_BYTES_MB = "totalBytesMB";
    public static final String KEY_PROGRESS_BAR_DETERMINATE = "setProgressbarAsDeterminate";

    private int downloadedPercent;

    public MyDownloadService() {
        super("MyDownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String downloadURL = intent.getStringExtra(AppDetailsActivity.KEY_DOWNLOAD_URL);
        startAppDownload(downloadURL);
    }

    private void startAppDownload(String downloadURL) {
        Uri uri = Uri.parse(downloadURL);

        DownloadManager.Request downloadRequest = new DownloadManager.Request(uri);
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        downloadRequest.setTitle(AppDetailsActivity.currentApp.getAppNameEng());
        downloadRequest.setVisibleInDownloadsUi(false);
        downloadRequest.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "store_downloads/"+AppDetailsActivity.currentApp.getAppNameEng() + ".apk"
        );

        DownloadManager downloadManager =
                (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        AppDetailsActivity.downloadID = downloadManager.enqueue(downloadRequest);

        displayDownloadProgress(AppDetailsActivity.downloadID);
    }

    private void displayDownloadProgress(final long download_id) {
        Intent updateUI = new Intent();
        int bytesDownloaded, totalBytes, downloadStatus;
        String totalBytesMB, bytesDownloadedMB;

        boolean isDownloading = true;

        DownloadManager downloadManager =
                (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query downloadQuery = new DownloadManager.Query();
        downloadQuery.setFilterById(download_id);

        Cursor cursor;

        // used to format downloaded bytes info text
        final DecimalFormat decimalFormat = new DecimalFormat("#.#");

        while (isDownloading) {

            cursor = downloadManager.query(downloadQuery);

            if(cursor.moveToFirst()) {
                bytesDownloaded = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                );

                totalBytes = cursor.getInt(
                        cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                );

                downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                // total percent downloaded
                if(totalBytes != 0) {
                    downloadedPercent = (int) ((bytesDownloaded * 100L) / totalBytes);
                }


                totalBytesMB = decimalFormat.format((float) totalBytes / 1000000);
                bytesDownloadedMB = decimalFormat.format((float) bytesDownloaded / 1000000);

                updateUI.setAction(BROADCAST_ACTION_DOWNLOAD_SERVICE);

                updateUI.putExtra(KEY_BYTES_DOWNLOADED_MB, bytesDownloadedMB);
                updateUI.putExtra(KEY_PERCENT_DOWNLOADED, downloadedPercent);
                updateUI.putExtra(KEY_TOTAL_BYTES_MB, totalBytesMB);

                if(totalBytes > 0) {
                    updateUI.putExtra(KEY_PROGRESS_BAR_DETERMINATE, true);
                }

                sendBroadcast(updateUI);

                cursor.close();

                if(downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                    isDownloading = false;
                }
            } else {
                // download canceled
                Intent downloadCancelled = new Intent();
                downloadCancelled.setAction(BROADCAST_ACTION_DOWNLOAD_CANCELLED);
                sendBroadcast(downloadCancelled);
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent allDownloadsCompleted = new Intent();
        allDownloadsCompleted.setAction(BROADCAST_ACTION_ALL_DOWNLOADS_COMPLETED);
        sendBroadcast(allDownloadsCompleted);
    }
}
