package com.creek.staccato.android.activity;

import java.io.IOException;
import java.util.Properties;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.creek.staccato.connector.mail.MailSessionKeeper;
import com.creek.staccato.connector.mail.PredefinedMailProperties;
import com.creek.staccato.domain.message.TransformException;
import com.creek.staccato.domain.profile.Profile;
import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.account.EmailAccountEditActivity;
import com.creek.staccato.android.activity.account.EmailAccountEditAdvancedActivity;
import com.creek.staccato.android.activity.group.GroupsListActivity;
import com.creek.staccato.android.activity.message.MessagesListActivity;
import com.creek.staccato.android.activity.profile.MyProfileActivity;
import com.creek.staccato.android.activity.profile.ProfilesListActivity;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.cache.CacheManager;
//import com.creek.staccato.android.services.repositoryservice.RepositoryMessagesCollector;
import com.creek.staccato.android.services.repositoryservice.Notificator;
import com.creek.staccato.android.services.repositoryservice.RepositoryService;
//import com.creek.staccato.android.services.repositoryservice.RepositoryServiceListener;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class StaccatoActivity extends Activity {
    // Initialization
    private static final int EMAIL_ACCOUNT_MENU_ITEM = Menu.FIRST;
    private static final int MY_PROFILE_MENU_ITEM = Menu.FIRST + 1;
    private static final int GROUPS_MENU_ITEM = Menu.FIRST + 2;
    private static final int PROFILES_MENU_ITEM = Menu.FIRST + 3;
    private static final int RECEIVE_MESSAGES_MENU_ITEM = Menu.FIRST + 4;
    private static final int HELP_MENU_ITEM = Menu.FIRST + 5;
    private static final String TAG = StaccatoActivity.class.getSimpleName();
    //private RepositoryMessagesCollector api;

//    private RepositoryServiceListener.Stub collectorListener = new RepositoryServiceListener.Stub() {
//        @Override
//        public void handleNewMessages() throws RemoteException {
//            // updateTweetView();
//        }
//    };
//
//    private ServiceConnection serviceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            Log.i(TAG, "Service connection established");
//
//            api = RepositoryMessagesCollector.Stub.asInterface(service);
//            try {
//                api.addListener(collectorListener);
//            } catch (RemoteException e) {
//                Log.e(TAG, "Failed to add listener", e);
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Log.i(TAG, "Service connection closed");
//        }
//    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(RepositoryService.class.getName());

        startService(intent);

        //bindService(intent, serviceConnection, 0);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu() called");
        try {
            Profile myProfile = CacheManager.getInstance().readMyProfileFromFile();
            if (myProfile != null) {
                menu.add(0, EMAIL_ACCOUNT_MENU_ITEM, 0, R.string.edit_email_account);
                menu.add(0, MY_PROFILE_MENU_ITEM, 0, R.string.my_profile);
                menu.add(0, GROUPS_MENU_ITEM, 0, R.string.groups);
                menu.add(0, PROFILES_MENU_ITEM, 0, R.string.profiles);
                menu.add(0, RECEIVE_MESSAGES_MENU_ITEM, 0, R.string.receive_messages);
            } else {
                menu.add(0, EMAIL_ACCOUNT_MENU_ITEM, 0, R.string.enter_email_account);
            }
            menu.add(0, HELP_MENU_ITEM, 0, R.string.help);
        } catch (TransformException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
            ActivityUtil.showException(this, ex);
        } catch (Throwable ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
        case EMAIL_ACCOUNT_MENU_ITEM:
            try {
                Profile myProfile = CacheManager.getInstance().readMyProfileFromFile();
                if (myProfile != null) {
                    try {
                        Properties props = ApplManager.getInstance().getMailProperties();
                        if (PredefinedMailProperties.getPredefinedProperties(props.getProperty(MailSessionKeeper.MAIL_USERNAME)) != null) {
                            intent = new Intent(StaccatoActivity.this, EmailAccountEditActivity.class);
                        } else {
                            intent = new Intent(StaccatoActivity.this, EmailAccountEditAdvancedActivity.class);
                        }
                    } catch (IOException ex) {
                        ActivityUtil.showException(this, ex);
                        return true;
                    }
                } else {
                    intent = new Intent(StaccatoActivity.this, EmailAccountEditActivity.class);
                }
                startActivity(intent);
            } catch (TransformException ex) {
                ActivityUtil.printStackTrace(TAG, ex);
                ActivityUtil.showException(this, ex);
            } catch (Throwable ex) {
                ActivityUtil.printStackTrace(TAG, ex);
            }
            return true;
        case MY_PROFILE_MENU_ITEM:
            intent = new Intent(StaccatoActivity.this, MyProfileActivity.class);
            startActivity(intent);
            return true;
        case GROUPS_MENU_ITEM:
            intent = new Intent(StaccatoActivity.this, GroupsListActivity.class);
            startActivity(intent);
            return true;
        case PROFILES_MENU_ITEM:
            intent = new Intent(StaccatoActivity.this, ProfilesListActivity.class);
            startActivity(intent);
            return true;
        case RECEIVE_MESSAGES_MENU_ITEM:
            removeNotification();
            intent = new Intent(StaccatoActivity.this, MessagesListActivity.class);
            startActivity(intent);
            return true;
        case HELP_MENU_ITEM:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        try {
//            api.removeListener(collectorListener);
//            unbindService(serviceConnection);
//        } catch (Throwable t) {
//            // catch any issues, typical for destroy routines
//            // even if we failed to destroy something, we need to continue
//            // destroying
//            Log.w(TAG, "Failed to unbind from the service", t);
//        }

        Log.i(TAG, "Activity destroyed");
    }
    
    private void removeNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.cancel(Notificator.STACCATO_NOTIFICATION_ID);
    }
}
