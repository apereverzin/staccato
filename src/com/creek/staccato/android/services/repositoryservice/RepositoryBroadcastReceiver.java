package com.creek.staccato.android.services.repositoryservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 
 * @author Andrey Pereverzin
 *
 */
public class RepositoryBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(RepositoryService.class.getName());
        context.startService(serviceIntent);
    }
}
