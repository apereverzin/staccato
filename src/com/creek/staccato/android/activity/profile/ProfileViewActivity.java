package com.creek.staccato.android.activity.profile;

import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.domain.profile.Profile;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class ProfileViewActivity extends Activity {
    private TextView emailView;
    private TextView firstNameView;
    private TextView lastNameView;
    private TextView nickNameView;
    private TextView commentView;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.profile_view);
        emailView = (TextView) findViewById(R.id.profile_email);
        firstNameView = (TextView) findViewById(R.id.profile_first_name);
        lastNameView = (TextView) findViewById(R.id.profile_last_name);
        nickNameView = (TextView) findViewById(R.id.profile_nick_name);
        commentView = (TextView) findViewById(R.id.profile_comment);
        closeButton = (Button) findViewById(R.id.group_save);

        Bundle extras = getIntent().getExtras();

        final Profile profile;

        if (extras.get(ProfilesListActivity.PROFILE) == null) {
            profile = null;
        } else {
            profile = (Profile) extras.get(ProfilesListActivity.PROFILE);
            emailView.setText(profile.getProfileKey().getEmailAddress());
            firstNameView.setText(profile.getFirstName());
            lastNameView.setText(profile.getLastName());
            nickNameView.setText(profile.getNickName());
            commentView.setText(profile.getMobilePhone());
        }

        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });

        StringBuilder title = ActivityUtil.buildActivityTitle(this, R.string.view_profile_activity_name);
        setTitle(title);
    }
}
