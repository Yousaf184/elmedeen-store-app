package com.example.yousafkhan.elmedeenappstore.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.yousafkhan.elmedeenappstore.activities.AppDetailsActivity;
import com.example.yousafkhan.elmedeenappstore.services.MyDownloadService;

// executes when all of the downloads have been completed by the MyDownloadService
public class AllDownloadRequestsCompleteReceiver extends BroadcastReceiver {

    private Context appDetailActivityContext;

    public AllDownloadRequestsCompleteReceiver(Context context) {
        appDetailActivityContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if(action != null && action.equals(MyDownloadService.BROADCAST_ACTION_ALL_DOWNLOADS_COMPLETED)) {
            ((AppDetailsActivity) appDetailActivityContext).unregisterDownloadReceivers();
        }
    }
}
