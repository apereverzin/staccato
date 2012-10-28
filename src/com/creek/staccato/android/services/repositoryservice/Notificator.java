package com.creek.staccato.android.services.repositoryservice;

import java.io.IOException;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.StaccatoActivity;
import com.creek.staccato.android.cache.CacheManager;
import com.creek.staccato.domain.BusinessException;
import com.creek.staccato.domain.message.GroupMembershipInvitationRequest;
import com.creek.staccato.domain.message.GroupMembershipInvitationResponse;
import com.creek.staccato.domain.message.GroupMembershipRequest;
import com.creek.staccato.domain.message.GroupMembershipResponse;
import com.creek.staccato.domain.message.GroupMembershipVoteRequest;
import com.creek.staccato.domain.message.GroupMembershipVoteResponse;
import com.creek.staccato.domain.message.GroupOwnershipInvitationRequest;
import com.creek.staccato.domain.message.GroupOwnershipInvitationResponse;
import com.creek.staccato.domain.message.InformationMessage;
import com.creek.staccato.domain.message.LocationMessage;
import com.creek.staccato.domain.message.generic.AddressedMessage;

/**
 * 
 * @author Andrey Pereverzin
 */
public class Notificator {
    private NotificationManager notificationManager;
    private Context context;
    
    public static final int STACCATO_NOTIFICATION_ID = 20;

    private static final String TAG = Notificator.class.getSimpleName();
    
    public Notificator(NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;
    }

    public void notifyUser(RepositoryService repositoryService, Set<? extends AddressedMessage> messages) throws BusinessException {
        int informationMessageCount = 0;
        int serviceMessageCount = 0;
        for(AddressedMessage message: messages) {
            Log.d(TAG, "notifyMe: " + message.getClass().getName());
            Log.d(TAG, "notifyMe: " + message.getMessageType());
            if (message instanceof InformationMessage) {
                ApplManager.getInstance().getMessageService().saveInformationMessage((InformationMessage)message);
                informationMessageCount++;
            }
            
            if (message instanceof LocationMessage || message instanceof GroupMembershipInvitationRequest || 
                    message instanceof GroupMembershipRequest || message instanceof GroupMembershipVoteRequest || 
                    message instanceof GroupOwnershipInvitationRequest || 
                    message instanceof GroupMembershipInvitationResponse || 
                    message instanceof GroupMembershipResponse || 
                    message instanceof GroupMembershipVoteResponse || 
                    message instanceof GroupOwnershipInvitationResponse) {
                serviceMessageCount++;
            }
        }
        
        Log.d(TAG, "informationMessageCount: " + informationMessageCount);
        Log.d(TAG, "serviceMessageCount: " + serviceMessageCount);
        if(informationMessageCount > 0 || serviceMessageCount > 0) {
            try {
                displayNotification(repositoryService);
            } catch(IOException ex) {
                throw new BusinessException(ex);
            }
        }
    }

    private void displayNotification(RepositoryService repositoryService) throws IOException {
        int icon = R.drawable.staccato_notification;        // icon from resources
        CharSequence tickerText = context.getString(R.string.notifications_staccato_messages); // ticker-text
        long when = System.currentTimeMillis();         // notification time

        // the next two lines initialize the Notification, using the configurations above
        Notification notification = new Notification(icon, tickerText, when);
        
        CharSequence contentTitle = context.getString(R.string.notifications_staccato_messages);  // message title
        String msg = context.getString(R.string.notifications_staccato_messages_received) + ": " +
                        CacheManager.getInstance().countInformationMessages();
        CharSequence contentText = msg; // message
                                                                                                                                                          // text
        //Intent notificationIntent = new Intent(repositoryService, RepositoryService.class);
        //PendingIntent contentIntent = PendingIntent.getActivity(repositoryService, 0, notificationIntent, 0);
        Intent notificationIntent = new Intent(context, StaccatoActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notificationManager.notify(STACCATO_NOTIFICATION_ID, notification);
    }
}
