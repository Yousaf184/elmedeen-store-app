package com.example.yousafkhan.elmedeenappstore.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.yousafkhan.elmedeenappstore.activities.AppDetailsActivity;
import com.example.yousafkhan.elmedeenappstore.services.MyDownloadService;

// updates UI
// receives broadcasts from background service while download is in progress
public class DownloadProgressReceiver extends BroadcastReceiver {

    private int percentDownloaded;
    private String totalBytesMB, bytesDownloadedMB;
    private boolean setDownloadProgbarAsDeterminate;

    private Context appDetailActivityContext;

    public DownloadProgressReceiver(Context context) {
        this.appDetailActivityContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action != null && action.equals(MyDownloadService.BROADCAST_ACTION_DOWNLOAD_SERVICE)) {

            bytesDownloadedMB = intent.getStringExtra(MyDownloadService.KEY_BYTES_DOWNLOADED_MB);
            totalBytesMB = intent.getStringExtra(MyDownloadService.KEY_TOTAL_BYTES_MB);
            percentDownloaded = intent.getIntExtra(MyDownloadService.KEY_PERCENT_DOWNLOADED, -1);
            setDownloadProgbarAsDeterminate = intent
                    .getBooleanExtra(MyDownloadService.KEY_PROGRESS_BAR_DETERMINATE, false);

            ((AppDetailsActivity) appDetailActivityContext).displayDownloadProgress(
                    bytesDownloadedMB, totalBytesMB, percentDownloaded, setDownloadProgbarAsDeterminate
            );
        }
    }
}
