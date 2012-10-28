package com.creek.staccato.android.activity.profile;

import com.creek.staccato.android.activity.R;
import com.creek.staccato.domain.profile.Profile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class ProfileEditActivity extends Activity {
    private EditText firstNameText;
    private EditText lastNameText;
    private EditText nickNameText;
    private EditText emailText;
    private Button saveButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.group_edit);
        firstNameText = (EditText) findViewById(R.id.profile_first_name);
        lastNameText = (EditText) findViewById(R.id.profile_last_name);
        nickNameText = (EditText) findViewById(R.id.profile_nick_name);
        emailText = (EditText) findViewById(R.id.profile_email);
        saveButton = (Button) findViewById(R.id.group_save);
        cancelButton = (Button) findViewById(R.id.group_cancel);

        Bundle extras = getIntent().getExtras();

        final Profile profile;

        if (extras == null || extras.get(ProfilesListActivity.PROFILE) == null) {
            profile = null;
        } else {
            profile = (Profile) extras.get(ProfilesListActivity.PROFILE);
            firstNameText.setText(profile.getFirstName());
            lastNameText.setText(profile.getLastName());
            nickNameText.setText(profile.getNickName());
            emailText.setText(profile.getProfileKey().getEmailAddress());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

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
        
        StringBuilder title = new StringBuilder(getString(R.string.app_name)).append(": ").append(getString(R.string.group_activity_name));
        setTitle(title);
    }
}
