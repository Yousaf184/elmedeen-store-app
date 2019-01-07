package com.example.yousafkhan.elmedeenappstore.BroadcastReceivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.yousafkhan.elmedeenappstore.activities.AppDetailsActivity;

public class DownloadCompleteReceiver extends BroadcastReceiver {

    private Context appDetailActivityContext;

    public DownloadCompleteReceiver(Context context) {
        appDetailActivityContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long downloadID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        String action = intent.getAction();

        if((action != null && action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                && (downloadID == AppDetailsActivity.downloadID)) {

            // install the downloaded apk file
            DownloadManager downloadManager =
                    (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            Uri uri = downloadManager.getUriForDownloadedFile(downloadID);

            Intent installApp = new Intent();
            installApp.setAction(Intent.ACTION_INSTALL_PACKAGE);
            installApp.setData(uri);
            installApp.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if(installApp.resolveActivity(context.getPackageManager()) != null) {
                appDetailActivityContext.startActivity(installApp);
            }

            // reset downloadID to -1
            AppDetailsActivity.downloadID = -1;
        }
    }
}
