package com.creek.staccato.android.activity.account;

import java.io.IOException;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import com.creek.staccato.connector.mail.ConnectorException;
import com.creek.staccato.connector.mail.MailMessageConnector;
import com.creek.staccato.connector.mail.MailSessionKeeper;
import com.creek.staccato.connector.mail.PredefinedMailProperties;
import com.creek.staccato.domain.repositorymessage.RepositoryException;
import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.activity.util.CryptoException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class EmailAccountEditActivity extends Activity {
    private static final String TAG = EmailAccountEditActivity.class.getSimpleName();

    private EditText emailAddressText;
    private EditText passwordText;
    private Button testButton;
    private Button advancedButton;
    private Button saveButton;
    
    static final String MAIL_PROPERTIES = "MAIL_PROPERTIES";

    @Override
    protected void onCreate(Bundle icicle) {
        Log.i(TAG, "onCreate() called");
        super.onCreate(icicle);
        setContentView(R.layout.mail_properties);
        emailAddressText = (EditText) findViewById(R.id.mail_username);
        passwordText = (EditText) findViewById(R.id.mail_password);
        testButton = (Button) findViewById(R.id.mail_properties_button_test);
        advancedButton = (Button) findViewById(R.id.mail_properties_button_advanced);
        saveButton = (Button) findViewById(R.id.mail_properties_button_save);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        try {
            Properties props = ApplManager.getInstance().getMailProperties();
            if (props != null) {
                emailAddressText.setText(props.getProperty(MailSessionKeeper.MAIL_USERNAME));
                passwordText.setText(props.getProperty(MailSessionKeeper.MAIL_PASSWORD));
            }
        } catch (IOException ex) {
            ActivityUtil.showException(this, ex);
        } catch (CryptoException ex) {
            ActivityUtil.showException(this, ex);
        }

        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "-----testButton clicked");
                String emailAddress = emailAddressText.getText().toString().toLowerCase();
                Properties fullProps = PredefinedMailProperties.getPredefinedProperties(emailAddress);
                if (fullProps != null) {
                    fullProps.setProperty(MailSessionKeeper.MAIL_USERNAME, emailAddress);
                    fullProps.setProperty(MailSessionKeeper.MAIL_PASSWORD, passwordText.getText().toString());
                    final Bundle bundle = new Bundle();
                    bundle.putSerializable(MAIL_PROPERTIES, fullProps);
                    Intent intent = new Intent(EmailAccountEditActivity.this, CheckEmailResultActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    // TODO no full props dialog
                    setResult(RESULT_OK);
                }
            }
        });

        advancedButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(TAG, "-----advancedButton clicked");
                String emailAddress = emailAddressText.getText().toString().toLowerCase();
                Properties fullProps = PredefinedMailProperties.getPredefinedProperties(emailAddress);
                if (fullProps == null) {
                    fullProps = new Properties();
                }
                fullProps.setProperty(MailSessionKeeper.MAIL_USERNAME, emailAddress);
                fullProps.setProperty(MailSessionKeeper.MAIL_PASSWORD, passwordText.getText().toString());
                final Bundle bundle = new Bundle();
                bundle.putSerializable(MAIL_PROPERTIES, fullProps);
                Intent intent = new Intent(EmailAccountEditActivity.this, EmailAccountEditAdvancedActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.i(TAG, "saveButton");
                try {
                    String emailAddress = emailAddressText.getText().toString().toLowerCase();
                    Properties fullProps = PredefinedMailProperties.getPredefinedProperties(emailAddress);
                    
                    Properties props = new Properties();
                    props.setProperty(MailSessionKeeper.MAIL_USERNAME, emailAddress);
                    props.setProperty(MailSessionKeeper.MAIL_PASSWORD, passwordText.getText().toString());
                    
                    if (fullProps != null) {
                        fullProps.setProperty(MailSessionKeeper.MAIL_USERNAME, emailAddress);
                        fullProps.setProperty(MailSessionKeeper.MAIL_PASSWORD, passwordText.getText().toString());
                        MailMessageConnector connector = new MailMessageConnector(fullProps);
                        ApplManager.getInstance().checkConnector(connector);
                        
                        ApplManager.getInstance().persistProperties(props);
                    } else {
                        final Bundle bundle = new Bundle();
                        bundle.putSerializable(MAIL_PROPERTIES, props);
                        Intent intent = new Intent(EmailAccountEditActivity.this, EmailAccountEditAdvancedActivity.class);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, 0);
                    }

                    setResult(RESULT_OK);
                    finish();
                } catch (RepositoryException ex) {
                    ActivityUtil.processCheckConnectionFailure(EmailAccountEditActivity.this, ex);
                } catch (IOException ex) {
                    ActivityUtil.showException(EmailAccountEditActivity.this, ex);
                } catch (CryptoException ex) {
                    ActivityUtil.showException(EmailAccountEditActivity.this, ex);
                }
            }
        });

        StringBuilder title = new StringBuilder(getString(R.string.app_name)).append(": ").append(getString(R.string.mail_properties_activity_name));
        setTitle(title);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            finish();
        }
    }
}
