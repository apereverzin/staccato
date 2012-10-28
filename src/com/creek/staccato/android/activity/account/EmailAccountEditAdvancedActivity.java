package com.creek.staccato.android.activity.account;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.activity.util.CryptoException;
import com.creek.staccato.connector.mail.MailMessageConnector;
import com.creek.staccato.connector.mail.MailSessionKeeper;
import com.creek.staccato.domain.repositorymessage.RepositoryException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class EmailAccountEditAdvancedActivity extends Activity {
    private static final String TAG = EmailAccountEditAdvancedActivity.class.getSimpleName();

    private EditText emailAddressText;
    private EditText passwordText;
    private EditText smtpHostText;
    private EditText smtpPortText;
    private EditText imapHostText;
    private EditText imapPortText;
    private CheckBox smtpAuthCheck;
    private CheckBox startTlsEnableCheck;
    private EditText smtpSocketFactoryPortText;
    private EditText smtpSocketFactoryClassText;
    private Button testButton;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.mail_properties_advanced);
        
        Bundle extras = getIntent().getExtras();
        @SuppressWarnings("unchecked")
        Map<String, String> fullMailProps = (Map<String, String>) extras.get(EmailAccountEditActivity.MAIL_PROPERTIES);
        Properties props = new Properties();
        props.putAll(fullMailProps);

        emailAddressText = (EditText) findViewById(R.id.mail_username);
        passwordText = (EditText) findViewById(R.id.mail_password);
        smtpHostText = (EditText) findViewById(R.id.mail_smtp_host);
        smtpPortText = (EditText) findViewById(R.id.mail_smtp_port);
        imapHostText = (EditText) findViewById(R.id.mail_imap_host);
        imapPortText = (EditText) findViewById(R.id.mail_imap_port);
        smtpAuthCheck = (CheckBox) findViewById(R.id.mail_smtp_auth);
        startTlsEnableCheck = (CheckBox) findViewById(R.id.mail_smtp_start_tls_enable);
        smtpSocketFactoryClassText = (EditText) findViewById(R.id.mail_smtp_socket_factory_class);
        smtpSocketFactoryPortText = (EditText) findViewById(R.id.mail_smtp_socket_factory_port);
        testButton = (Button) findViewById(R.id.mail_properties_button_test);
        saveButton = (Button) findViewById(R.id.mail_properties_button_save);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        emailAddressText.setText(props.getProperty(MailSessionKeeper.MAIL_USERNAME));
        passwordText.setText(props.getProperty(MailSessionKeeper.MAIL_PASSWORD));
        smtpHostText.setText(props.getProperty(MailSessionKeeper.MAIL_SMTP_HOST));
        smtpPortText.setText(props.getProperty(MailSessionKeeper.MAIL_SMTP_PORT));
        smtpAuthCheck.setChecked("true".equalsIgnoreCase(props.getProperty(MailSessionKeeper.MAIL_SMTP_AUTH)));
        startTlsEnableCheck.setChecked("true".equalsIgnoreCase(props.getProperty(MailSessionKeeper.MAIL_SMTP_STARTTLS_ENABLE)));
        smtpSocketFactoryClassText.setText(props.getProperty(MailSessionKeeper.MAIL_SMTP_SOCKET_FACTORY_CLASS));
        smtpSocketFactoryPortText.setText(props.getProperty(MailSessionKeeper.MAIL_SMTP_SOCKET_FACTORY_PORT));
        imapHostText.setText(props.getProperty(MailSessionKeeper.MAIL_IMAP_HOST));
        imapPortText.setText(props.getProperty(MailSessionKeeper.MAIL_IMAP_PORT));

        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "-----testButton clicked");
                try {
                    Properties fullProps = gatherProperties();
                    final Bundle bundle = new Bundle();
                    bundle.putSerializable(EmailAccountEditActivity.MAIL_PROPERTIES, fullProps);
                    Intent intent = new Intent(EmailAccountEditAdvancedActivity.this, CheckEmailResultActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } catch (RepositoryException ex) {
                    new AlertDialog.Builder(EmailAccountEditAdvancedActivity.this).setTitle("Error").setMessage(ex.getMessage()).setNeutralButton("Close", null).show();
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "-----saveButton clicked");
                try {
                    Properties fullProps = gatherProperties();
                    MailMessageConnector connector = new MailMessageConnector(fullProps);
                    ApplManager.getInstance().checkConnector(connector);
                    
                    ApplManager.getInstance().persistProperties(fullProps);
                    setResult(RESULT_OK);
                    finish();
                } catch (RepositoryException ex) {
                    ActivityUtil.processCheckConnectionFailure(EmailAccountEditAdvancedActivity.this, ex);
                } catch (IOException ex) {
                    ActivityUtil.showException(EmailAccountEditAdvancedActivity.this, ex);
                } catch (CryptoException ex) {
                    ActivityUtil.showException(EmailAccountEditAdvancedActivity.this, ex);
                }
            }
        });

        StringBuilder title = new StringBuilder(getString(R.string.app_name)).append(": ").append(getString(R.string.mail_properties_activity_name));
        setTitle(title);
    }
    
    private Properties gatherProperties() throws RepositoryException {
        Properties fullProps = new Properties();
        if(emailAddressText.getText() != null) {
            String emailAddress = emailAddressText.getText().toString().toLowerCase();
            fullProps.setProperty(MailSessionKeeper.MAIL_USERNAME, emailAddress);
        }
        gatherTextFieldValue(fullProps, MailSessionKeeper.MAIL_PASSWORD, passwordText);
        gatherTextFieldValue(fullProps, MailSessionKeeper.MAIL_SMTP_HOST, smtpHostText);
        gatherTextFieldValue(fullProps, MailSessionKeeper.MAIL_SMTP_PORT, smtpPortText);
        if(smtpAuthCheck.isChecked()) {
            fullProps.setProperty(MailSessionKeeper.MAIL_SMTP_AUTH, "true");
        }
        if(smtpAuthCheck.isChecked()) {
            fullProps.setProperty(MailSessionKeeper.MAIL_SMTP_STARTTLS_ENABLE, "true");
        }
        gatherTextFieldValue(fullProps, MailSessionKeeper.MAIL_SMTP_SOCKET_FACTORY_CLASS, smtpSocketFactoryClassText);
        gatherTextFieldValue(fullProps, MailSessionKeeper.MAIL_SMTP_SOCKET_FACTORY_PORT, smtpSocketFactoryPortText);
        gatherTextFieldValue(fullProps, MailSessionKeeper.MAIL_IMAP_HOST, imapHostText);
        gatherTextFieldValue(fullProps, MailSessionKeeper.MAIL_IMAP_PORT, imapPortText);
        return fullProps;
    }
    
    private void gatherTextFieldValue(Properties props, String propName, EditText field) {
        if(field.getText() != null) {
            props.setProperty(propName, field.getText().toString());
        }
    }
}
