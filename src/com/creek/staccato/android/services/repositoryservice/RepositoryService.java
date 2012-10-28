package com.creek.staccato.android.services.repositoryservice;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.activity.util.CryptoException;
import com.creek.staccato.android.cache.CacheManager;
import com.creek.staccato.domain.BusinessException;
import com.creek.staccato.domain.group.Group;
import com.creek.staccato.domain.message.generic.AddressedMessage;
import com.creek.staccato.domain.profile.Profile;
import com.creek.staccato.domain.repositorymessage.RepositoryException;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 
 * @author Andrey Pereverzin
 */
public class RepositoryService extends Service {
    private static final String TAG = RepositoryService.class.getSimpleName();

    private Timer timer;
    private Notificator notificator;
    private MessagesProcessor messagesProcessor;
    private boolean initializedSuccessfully = false;
    
    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            Log.i(TAG, "-------------------Timer task doing work");
            // TODO Are these checks necessary
            // initializeIfNecessary();
            // if(initializedSuccessfully) {
            //     processMessages();
            // }
            processMessages();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        
        messagesProcessor = new MessagesProcessor();
        
        initializeIfNecessary();

        timer = new Timer("StaccatoTimer");
        timer.schedule(updateTask, 1000L, 1 * 1000L);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }

    private void initializeIfNecessary() {
        long l = System.currentTimeMillis();
        Log.i(TAG, "-------------------initializeIfNecessary");
        try {
            if(ApplManager.getInstance().checkIfInitialized()) {
                Log.i(TAG, "-------------------initializing");
                initializedSuccessfully = false;
                Set<Group> groups = ApplManager.getInstance().getGroupService().getGroups();
                CacheManager.getInstance().writeGroupsToFiles(groups);
                Profile myProfile = ApplManager.getInstance().getGroupService().getMyProfile();
                CacheManager.getInstance().writeMyProfileToFile(myProfile);
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
                notificator = new Notificator(notificationManager, getApplicationContext());
                initializedSuccessfully = true;
            }
            Log.i(TAG, "-----initializeIfNecessary finished true " + (System.currentTimeMillis() - l));
        } catch (IOException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        } catch (RepositoryException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        } catch (CryptoException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        } catch (Throwable ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        }
    }
    
    private void processMessages() {
        Log.i(TAG, "-------------------processMessages");
        try {
            messagesProcessor.executeMyCommands();
            Set<? extends AddressedMessage> messages = receiveMessages();
            messagesProcessor.processRepositoryMessages();
            notificator.notifyUser(RepositoryService.this, messages);
        } catch(IOException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        } catch(BusinessException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        }
    }
    
    private Set<? extends AddressedMessage> receiveMessages() throws BusinessException, IOException{
        Set<? extends AddressedMessage> messages = ApplManager.getInstance().getMessageService().getNewMessages();
        CacheManager.getInstance().writeMessagesToInputDir((Set<? extends AddressedMessage>) messages);
        Log.e(TAG, "------------------receiveMessages: " + messages.size());
        return messages;
    }
}
