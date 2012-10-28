package com.creek.staccato.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.util.Log;

import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.activity.util.CryptoException;
import com.creek.staccato.android.activity.util.CryptoUtil;
import com.creek.staccato.connector.mail.ConnectorException;
import com.creek.staccato.connector.mail.MailMessageConnector;
import com.creek.staccato.connector.mail.MailSessionKeeper;
import com.creek.staccato.connector.mail.PredefinedMailProperties;
import com.creek.staccato.domain.group.GroupRepository;
import com.creek.staccato.domain.group.GroupService;
import com.creek.staccato.domain.group.impl.GroupServiceImpl;
import com.creek.staccato.domain.message.InformationMessageRepository;
import com.creek.staccato.domain.message.MessageCommunicator;
import com.creek.staccato.domain.message.MessageRepository;
import com.creek.staccato.domain.message.MessageService;
import com.creek.staccato.domain.message.impl.MessageServiceImpl;
import com.creek.staccato.domain.profile.Profile;
import com.creek.staccato.domain.profile.ProfileKey;
import com.creek.staccato.domain.repositorymessage.RepositoryException;
import com.creek.staccato.repository.email.EmailGroupRepository;
import com.creek.staccato.repository.email.EmailInformationMessageRepository;
import com.creek.staccato.repository.email.EmailMessageRepository;
import com.creek.staccato.repository.email.MailMessageCommunicator;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public final class ApplManager {
    private static final String TAG = ApplManager.class.getSimpleName();
    private static ApplManager instance = new ApplManager();
    private Properties mailProperties = null;
    private GroupService groupService;
    private MessageService messageService;
    private static final String STACCATO_PROPERTIES_FILE_PATH = "/sdcard/Android/staccato.properties";
    private long configLastModified = 0l;

    private ApplManager() {
        //
    }

    public static ApplManager getInstance() {
        return instance;
    }

    public boolean checkIfInitialized() throws IOException, RepositoryException, CryptoException {
        long l = System.currentTimeMillis();
        Log.i(TAG, "-----initializeIfNecessary");
        File propertiesFile = new File(STACCATO_PROPERTIES_FILE_PATH);
        if(fileMofified(configLastModified, propertiesFile)) {
            Log.i(TAG, "-----initializing");
            Properties props = new Properties();
            props.load(new FileInputStream(propertiesFile));
            decryptPassword(props);
            
            MailMessageConnector connector = getConnector(props);

            checkConnector(connector);
            
            MessageRepository messageRepository = new EmailMessageRepository(connector, true);
            InformationMessageRepository informationMessageRepository = new EmailInformationMessageRepository(connector, true);
            GroupRepository groupRepository = new EmailGroupRepository(connector, false);
            
            setMyProfile(props, groupRepository);

            MessageCommunicator communicator = new MailMessageCommunicator(connector);

            messageService = new MessageServiceImpl();
            messageService.setMessageRepository(messageRepository);
            messageService.setInformationMessageRepository(informationMessageRepository);
            messageService.setMessageCommunicator(communicator);

            groupService = new GroupServiceImpl();
            groupService.setGroupRepository(groupRepository);
            configLastModified = propertiesFile.lastModified();
            
            Log.i(TAG, "-----initializeIfNecessary finished true " + (System.currentTimeMillis() - l));
            return true;
        }
        
        Log.i(TAG, "-----initializeIfNecessary finished false " + (System.currentTimeMillis() - l));
        return false;
    }

    public void persistProperties(Properties propsToPersist) throws IOException, CryptoException, RepositoryException {
        long l = System.currentTimeMillis();
        Log.i(TAG, "-----persistProperties");
        File f = new File(STACCATO_PROPERTIES_FILE_PATH);
        f.createNewFile();
        String password = propsToPersist.getProperty(MailSessionKeeper.MAIL_PASSWORD);
        String cryptPassword = CryptoUtil.encrypt("abcd", password);
        propsToPersist.setProperty(MailSessionKeeper.MAIL_PASSWORD, cryptPassword);
        propsToPersist.store(new FileOutputStream(f), "");
        propsToPersist.setProperty(MailSessionKeeper.MAIL_PASSWORD, password);
        mailProperties = propsToPersist;
        Log.i(TAG, "-----persistProperties finished " + (System.currentTimeMillis() - l));
    }

    public Properties getMailProperties() throws IOException, CryptoException {
        Log.i(TAG, "-----getMailProperties");
        if (mailProperties == null) {
            File f = new File(STACCATO_PROPERTIES_FILE_PATH);
            if (f.exists()) {
                mailProperties = new Properties();
                mailProperties.load(new FileInputStream(f));
                decryptPassword(mailProperties);
            }
        }
        return mailProperties;
    }

    public GroupService getGroupService() {
        return groupService;
    }

    public MessageService getMessageService() {
        return messageService;
    }
    
    public void checkConnector(MailMessageConnector conn) throws RepositoryException {
        try {
            conn.checkSMTPConnection();
            conn.checkIMAPConnection();
        } catch(ConnectorException ex) {
            throw new RepositoryException(ex);
        }
    }
    
    private boolean fileMofified(long lastModifiedCached, File file) {
        return lastModifiedCached != file.lastModified();
    }
    
    private void decryptPassword(Properties props) throws CryptoException {
        String cryptPassword = props.getProperty(MailSessionKeeper.MAIL_PASSWORD);
        String password = CryptoUtil.decrypt("abcd", cryptPassword);
        props.setProperty(MailSessionKeeper.MAIL_PASSWORD, password);
    }
    
    private void setMyProfile(Properties props, GroupRepository groupRepository) throws RepositoryException {
        long l = System.currentTimeMillis();
        Log.i(TAG, "-----setMyProfile");
        Profile myProfile = groupRepository.getMyProfile();
        Log.i(TAG, "-----setMyProfile1");
        if (myProfile == null) {
            String emailAddress = props.getProperty(MailSessionKeeper.MAIL_USERNAME);
            ProfileKey myProfileKey = new ProfileKey(emailAddress);
            myProfile = new Profile(myProfileKey);
            Log.i(TAG, "-----createMyProfile, emailAddress: " + emailAddress);
            groupRepository.createMyProfile(myProfile);
        }
        Log.i(TAG, "-----setMyProfile finished " + (System.currentTimeMillis() - l));
    }
    
    private MailMessageConnector getConnector(Properties props) {
        long l = System.currentTimeMillis();
        Log.i(TAG, "-----getConnector started");
        if (needSearchPredefinedProperties(props)) {
            String emailAddress = props.getProperty(MailSessionKeeper.MAIL_USERNAME);
            Properties fullProps = PredefinedMailProperties.getPredefinedProperties(emailAddress);

            if (fullProps != null) {
                fullProps.setProperty(MailSessionKeeper.MAIL_USERNAME, props.getProperty(MailSessionKeeper.MAIL_USERNAME));
                fullProps.setProperty(MailSessionKeeper.MAIL_PASSWORD, props.getProperty(MailSessionKeeper.MAIL_PASSWORD));
                ActivityUtil.printProperties(TAG, fullProps);
                props = fullProps;
            }
        }

        Log.i(TAG, "-----getConnector finished " + (System.currentTimeMillis() - l));
        return new MailMessageConnector(props);
    }
    
    private boolean needSearchPredefinedProperties(Properties props) {
        return !props.contains(MailSessionKeeper.MAIL_SMTP_HOST);
    }
}
