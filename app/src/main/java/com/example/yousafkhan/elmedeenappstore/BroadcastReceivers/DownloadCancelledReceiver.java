package com.example.yousafkhan.elmedeenappstore.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.yousafkhan.elmedeenappstore.activities.AppDetailsActivity;
import com.example.yousafkhan.elmedeenappstore.services.MyDownloadService;

public class DownloadCancelledReceiver extends BroadcastReceiver {

    private Context appDetailActivityContext;

    public DownloadCancelledReceiver(Context context) {
        this.appDetailActivityContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action != null && action.equals(MyDownloadService.BROADCAST_ACTION_DOWNLOAD_CANCELLED)) {
            ((AppDetailsActivity) appDetailActivityContext).changeAppState();

            // reset downloadID to -1
            // needed to change app detail activity state
            AppDetailsActivity.downloadID = -1;
        }
    }
}
