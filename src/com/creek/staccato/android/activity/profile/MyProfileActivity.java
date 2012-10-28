package com.creek.staccato.android.activity.profile;

import java.io.IOException;
import java.util.Properties;

import com.creek.staccato.connector.mail.MailSessionKeeper;
import com.creek.staccato.domain.message.TransformException;
import com.creek.staccato.domain.profile.Profile;
import com.creek.staccato.domain.profile.ProfileKey;
import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.activity.util.CryptoException;
import com.creek.staccato.android.cache.CacheManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class MyProfileActivity extends Activity {
    private static final String TAG = MyProfileActivity.class.getSimpleName();

    private TextView emailText;
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText nickNameText;
    private EditText commentText;
    private Button saveButton;
    private Button cancelButton;
    private Profile myProfile;

    @Override
    protected void onCreate(Bundle icicle) {
        Log.e(TAG, "onCreate() called");
        super.onCreate(icicle);
        setContentView(R.layout.my_profile);
        emailText = (TextView) findViewById(R.id.profile_email);
        firstNameText = (EditText) findViewById(R.id.profile_first_name);
        firstNameText = (EditText) findViewById(R.id.profile_first_name);
        lastNameText = (EditText) findViewById(R.id.profile_last_name);
        nickNameText = (EditText) findViewById(R.id.profile_nick_name);
        commentText = (EditText) findViewById(R.id.profile_comment);
        saveButton = (Button) findViewById(R.id.profile_button_save);
        cancelButton = (Button) findViewById(R.id.profile_button_cancel);

        try {
            emailText.setText(ApplManager.getInstance().getMailProperties().getProperty(MailSessionKeeper.MAIL_USERNAME));
            Profile myProfile = CacheManager.getInstance().readMyProfileFromFile();
            if (myProfile != null) {
                firstNameText.setText(myProfile.getFirstName());
                lastNameText.setText(myProfile.getLastName());
                nickNameText.setText(myProfile.getNickName());
                commentText.setText(myProfile.getComment());
            } else {
                firstNameText.setText("");
                lastNameText.setText("");
                nickNameText.setText("");
                commentText.setText("");
            }
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
            ActivityUtil.showException(this, ex);
        } catch (TransformException ex) {
            Log.e(TAG, ex.getMessage());
            ActivityUtil.showException(this, ex);
        } catch (CryptoException ex) {
            Log.e(TAG, ex.getMessage());
            ActivityUtil.showException(this, ex);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    if (myProfile == null) {
                        ProfileKey profileKey = new ProfileKey(ApplManager.getInstance().getMailProperties().getProperty(MailSessionKeeper.MAIL_USERNAME));
                        myProfile = new Profile(profileKey);
                        myProfile.setFirstName(firstNameText.getText().toString());
                        myProfile.setLastName(lastNameText.getText().toString());
                        myProfile.setNickName(nickNameText.getText().toString());
                        myProfile.setComment(commentText.getText().toString());
                    } else {
                        myProfile.setFirstName(firstNameText.getText().toString());
                        myProfile.setLastName(lastNameText.getText().toString());
                        myProfile.setNickName(nickNameText.getText().toString());
                        myProfile.setComment(commentText.getText().toString());
                    }
                    CacheManager.getInstance().addOrUpdateMyProfile(myProfile);
                } catch (IOException ex) {
                    ActivityUtil.showException(MyProfileActivity.this, ex);
                } catch (CryptoException ex) {
                    ActivityUtil.showException(MyProfileActivity.this, ex);
                }
                setResult(RESULT_OK);
                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });

        StringBuilder title = new StringBuilder(getString(R.string.app_name)).append(": ").append(getString(R.string.my_profile_activity_name));
        setTitle(title);
    }

    protected boolean testEmailAddress(Properties props) {
        return true;
    }
}
