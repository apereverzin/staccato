package com.creek.staccato.android.services.repositoryservice;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;

import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.cache.CacheManager;
import com.creek.staccato.domain.BusinessException;
import com.creek.staccato.domain.group.Group;
import com.creek.staccato.domain.message.InformationMessage;
import com.creek.staccato.domain.message.TransformException;
import com.creek.staccato.domain.message.generic.AddressedMessage;
import com.creek.staccato.domain.message.generic.GenericMessage;
import com.creek.staccato.domain.message.generic.MultipleGroupMessage;
import com.creek.staccato.domain.message.generic.MultipleProfileMessage;
import com.creek.staccato.domain.message.generic.Request;
import com.creek.staccato.domain.message.generic.Response;
import com.creek.staccato.domain.message.generic.SingleProfileMessage;
import com.creek.staccato.domain.profile.Profile;
import com.creek.staccato.domain.repositorymessage.RepositoryGroup;
import com.creek.staccato.domain.repositorymessage.RepositoryMessage;
import com.creek.staccato.domain.repositorymessage.RepositoryProfile;
import com.creek.staccato.domain.servicemessage.GroupDeleted;
import com.creek.staccato.domain.servicemessage.GroupUpdated;
import com.creek.staccato.domain.servicemessage.ProfileAddedToGroup;
import com.creek.staccato.domain.servicemessage.ProfileDeleted;
import com.creek.staccato.domain.servicemessage.ProfileUpdated;

/**
 * 
 * @author Andrey Pereverzin
 *
 */
public class MessagesProcessor {
    private static final String TAG = MessagesProcessor.class.getSimpleName();

    public void executeMyCommands() {
        Log.d(TAG, "executeMyCommands");
        try {
            Set<GenericMessage> messages = CacheManager.getInstance().readMessagesFromOutput();
            for (GenericMessage message : messages) {
                if (message instanceof RepositoryMessage) {
                    executeRepositoryCommand((RepositoryMessage)message);
                } else {
                    sendAddressedMessage((AddressedMessage)message);
                }
            }
        } catch (TransformException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        } catch (BusinessException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        }
    }
    
    public void processRepositoryMessages() throws IOException {
        try {
            Set<GenericMessage> allMessages = CacheManager.getInstance().readGenericMessagesFromInputDir();
            Set<InformationMessage> informationMessages = new HashSet<InformationMessage>();
            Set<Request> requests = new HashSet<Request>();
            Set<Response> responses = new HashSet<Response>();
            Set<MultipleGroupMessage> repositoryMessages = new HashSet<MultipleGroupMessage>();
            for (GenericMessage message : allMessages) {
                if (message instanceof GroupUpdated || message instanceof GroupDeleted || message instanceof ProfileAddedToGroup || message instanceof ProfileUpdated || message instanceof ProfileDeleted) {
                    repositoryMessages.add((MultipleGroupMessage)message);
                } else if (message instanceof InformationMessage) {
                    informationMessages.add((InformationMessage)message);
                } else if (message instanceof Request) {
                    requests.add((Request)message);
                } else if (message instanceof Response) {
                    responses.add((Response)message);
                }
            }
            CacheManager.getInstance().writeMessagesToRepositoryMessagesDir(repositoryMessages);
            CacheManager.getInstance().writeMessagesToInformationMessagesDir(informationMessages);
            CacheManager.getInstance().writeMessagesToRequestsDir(requests);
            CacheManager.getInstance().writeMessagesToResponsesDir(responses);
        } catch (TransformException ex) {
            ActivityUtil.printStackTrace(TAG, ex);
        }
    }
    
    private void executeRepositoryCommand(RepositoryMessage message) throws TransformException, BusinessException {
        Log.d(TAG, "executeRepositoryCommand");
        if (message instanceof RepositoryGroup) {
            Group group = ((RepositoryGroup) message).getData();
            if (ApplManager.getInstance().getGroupService().getGroup(group.getGroupKey()) == null) {
                ApplManager.getInstance().getGroupService().createGroup(group);
            } else {
                ApplManager.getInstance().getGroupService().updateGroup(group);
            }
        } else if (message instanceof RepositoryProfile) {
            Profile profile = ((RepositoryProfile) message).getData();
            ApplManager.getInstance().getGroupService().updateMyProfile(profile);
        }
    }

    private void sendAddressedMessage(AddressedMessage message) throws TransformException, BusinessException {
        Log.d(TAG, "sendAddressedMessage");
        if(message instanceof SingleProfileMessage) {
            //
        } else if(message instanceof MultipleProfileMessage) {
            //
        } else {
            throw new IllegalArgumentException("Wrong message type to send");
        }
    }
}
