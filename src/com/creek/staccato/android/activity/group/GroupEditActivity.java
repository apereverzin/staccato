package com.creek.staccato.android.activity.group;

import java.io.IOException;

import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.cache.CacheManager;
import com.creek.staccato.domain.group.Group;
import com.creek.staccato.domain.group.GroupKey;
import com.creek.staccato.domain.message.TransformException;
import com.creek.staccato.domain.profile.Profile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class GroupEditActivity extends Activity {
    private static final String TAG = GroupEditActivity.class.getSimpleName();

    private EditText nameText;
    private EditText descriptionText;
    private Button saveButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.group_edit);
        nameText = (EditText) findViewById(R.id.group_name);
        descriptionText = (EditText) findViewById(R.id.group_description);
        saveButton = (Button) findViewById(R.id.group_save);
        cancelButton = (Button) findViewById(R.id.group_cancel);

        Bundle extras = getIntent().getExtras();

        final Group group;

        if (extras == null || extras.get(GroupsListActivity.GROUP) == null) {
            group = null;
        } else {
            group = (Group) extras.get(GroupsListActivity.GROUP);
            nameText.setText(group.getGroupKey().getName());
            descriptionText.setText(group.getDescription());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    Profile myProfile = CacheManager.getInstance().readMyProfileFromFile();
                    if (group == null) {
                        GroupKey groupKey = new GroupKey(nameText.getText().toString(), myProfile.getProfileKey());
                        Group newGroup = new Group(groupKey);
                        newGroup.setDescription(descriptionText.getText().toString());
                        CacheManager.getInstance().addOrUpdateGroup(newGroup);
                    } else {
                        group.setDescription(descriptionText.getText().toString());
                        CacheManager.getInstance().addOrUpdateGroup(group);
                    }
                } catch (TransformException ex) {
                    Log.e(TAG, ex.getMessage());
                    ActivityUtil.showException(GroupEditActivity.this, ex);
                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage());
                    ActivityUtil.showException(GroupEditActivity.this, ex);
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
        
        StringBuilder title = new StringBuilder(getString(R.string.app_name)).append(": ").append(getString(R.string.group_activity_name));
        setTitle(title);
    }
}
