package com.creek.staccato.android.activity.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.creek.staccato.android.ApplManager;
import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.group.GroupsListActivity;
import com.creek.staccato.android.activity.message.MessagesListActivity;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.domain.BusinessException;
import com.creek.staccato.domain.group.Group;
import com.creek.staccato.domain.profile.Profile;
import com.creek.staccato.domain.profile.ProfileKey;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * List of all profiles.
 * 
 * @author Andrey Pereverzin
 */
public class ProfilesListActivity extends ListActivity {
    private static final String TAG = ProfilesListActivity.class.getSimpleName();

    private static final int CREATE_PROFILE_MENU_ITEM = Menu.FIRST;

    private static final int VIEW_MESSAGES_MENU_ITEM = Menu.FIRST;
    private static final int VIEW_PROFILE_MENU_ITEM = Menu.FIRST + 1;
    private static final int UPDATE_PROFILE_MENU_ITEM = Menu.FIRST + 2;
    private static final int DELETE_PROFILE_MENU_ITEM = Menu.FIRST + 3;

    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";

    public static final String PROFILE = "PROFILE";

    private List<Map<String, String>> profilesList;
    private List<Profile> profilesDataList;
    private SimpleAdapter profilesListAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profiles_list);
        Bundle bundle = getIntent().getExtras();
        Group group;

        try {
            profilesList = new LinkedList<Map<String, String>>();
            profilesDataList = new ArrayList<Profile>();
            
            Set<ProfileKey> profileKeys;
            if(bundle != null) {
                group = (Group)bundle.getSerializable(GroupsListActivity.GROUP);
                profileKeys = new HashSet<ProfileKey>(ApplManager.getInstance().getGroupService().getGroupProfileKeys(group.getGroupKey()));
            } else {
                profileKeys = new HashSet<ProfileKey>(ApplManager.getInstance().getGroupService().getFreeProfileKeys());
            }
            
            for(ProfileKey profileKey: profileKeys) {
                profilesDataList.add(ApplManager.getInstance().getGroupService().getProfile(profileKey));
            }

            for (Profile profile : profilesDataList) {
                Map<String, String> profileMap = createProfileMapForList(profile);
                profilesList.add(profileMap);
            }
            profilesListAdapter = new SimpleAdapter(getApplicationContext(), profilesList, R.layout.profile_row, new String[] { FIRST_NAME, LAST_NAME }, new int[] { R.id.first_name, R.id.last_name });
            setListAdapter(profilesListAdapter);

            registerForContextMenu(getListView());
        } catch (BusinessException ex) {
            ActivityUtil.showException(this, ex);
        }

        StringBuilder title = ActivityUtil.buildActivityTitle(this, R.string.groups_activity_name);
        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu() called");
        menu.add(0, CREATE_PROFILE_MENU_ITEM, 0, R.string.menu_profiles_create_profile);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu()");
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, VIEW_MESSAGES_MENU_ITEM, 0, R.string.menu_profiles_view_messages);
        menu.add(0, UPDATE_PROFILE_MENU_ITEM, 0, R.string.menu_profiles_update_profile);
        menu.add(0, DELETE_PROFILE_MENU_ITEM, 0, R.string.menu_profiles_delete_profile);
        menu.add(0, VIEW_PROFILE_MENU_ITEM, 0, R.string.menu_profiles_view_profile);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CREATE_PROFILE_MENU_ITEM:
            Log.d(TAG, "CREATE_GROUP_MENU_ITEM");
            Intent tripsIntent = new Intent(ProfilesListActivity.this, ProfileEditActivity.class);
            startActivity(tripsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final Profile profileSelected = profilesDataList.get((int) info.id);
        switch (item.getItemId()) {
        case VIEW_MESSAGES_MENU_ITEM:
            Log.d(TAG, "VIEW_MESSAGES_MENU_ITEM");
            Intent tripsIntent = new Intent(ProfilesListActivity.this, MessagesListActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(PROFILE, profileSelected);
            tripsIntent.putExtras(bundle);
            startActivity(tripsIntent);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
    }

    private Map<String, String> createProfileMapForList(Profile profile) {
        Map<String, String> profileMap = new HashMap<String, String>();
        profileMap.put(FIRST_NAME, profile.getFirstName());
        profileMap.put(LAST_NAME, profile.getLastName());
        return profileMap;
    }
}
