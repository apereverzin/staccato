package com.creek.staccato.android.activity.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.message.MessagesListActivity;
import com.creek.staccato.android.activity.profile.ProfilesListActivity;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.cache.CacheManager;
import com.creek.staccato.domain.group.Group;
import com.creek.staccato.domain.message.TransformException;

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
 * List of all groups.
 * 
 * @author Andrey Pereverzin
 * 
 */
public class GroupsListActivity extends ListActivity {
    private static final String TAG = GroupsListActivity.class.getSimpleName();
    private static final int CREATE_GROUP_MENU_ITEM = Menu.FIRST;

    private static final int VIEW_PROFILES_MENU_ITEM = Menu.FIRST;
    private static final int VIEW_MESSAGES_MENU_ITEM = Menu.FIRST + 1;
    private static final int DELETE_GROUP_MENU_ITEM = Menu.FIRST + 2;

    private static final String GROUP_NAME = "group_name";

    public static final String GROUP = "GROUP";

    private List<Map<String, String>> groupsList;
    private List<Group> groupsDataList;
    private SimpleAdapter groupsListAdapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.groups_list);

        init();
        StringBuilder title = new StringBuilder(getString(R.string.app_name)).append(": ").append(getString(R.string.groups_activity_name));
        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        menu.add(0, CREATE_GROUP_MENU_ITEM, 0, R.string.menu_groups_create_group);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case CREATE_GROUP_MENU_ITEM:
            Log.d(TAG, "CREATE_GROUP_MENU_ITEM");
            Intent tripsIntent = new Intent(GroupsListActivity.this, GroupEditActivity.class);
            startActivity(tripsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu()");
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, VIEW_PROFILES_MENU_ITEM, 0, R.string.menu_groups_view_profiles);
        menu.add(0, VIEW_MESSAGES_MENU_ITEM, 0, R.string.menu_groups_view_messages);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final Group groupSelected = groupsDataList.get((int) info.id);
        final Bundle bundle = new Bundle();
        bundle.putSerializable(GROUP, groupSelected);
        Intent intent;
        switch (item.getItemId()) {
        case VIEW_PROFILES_MENU_ITEM:
            Log.d(TAG, "VIEW_PROFILES_MENU_ITEM");
            intent = new Intent(GroupsListActivity.this, ProfilesListActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        case VIEW_MESSAGES_MENU_ITEM:
            Log.d(TAG, "VIEW_MESSAGES_MENU_ITEM");
            intent = new Intent(GroupsListActivity.this, MessagesListActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        init();
        super.onResume();
    }
    
    private void init() {
        try {
            groupsList = new LinkedList<Map<String, String>>();
            groupsDataList = new ArrayList<Group>(CacheManager.getInstance().readGroupsFromGroupsDir());

            for (Group group : groupsDataList) {
                Map<String, String> groupMap = createGroupMapForList(group);
                groupsList.add(groupMap);
            }
            groupsListAdapter = new SimpleAdapter(getApplicationContext(), groupsList, R.layout.group_row, new String[] { GROUP_NAME }, new int[] { R.id.group_name });
            setListAdapter(groupsListAdapter);

            registerForContextMenu(getListView());
        } catch (TransformException ex) {
            ActivityUtil.showException(this, ex);
        }
    }

    private Map<String, String> createGroupMapForList(Group group) {
        Map<String, String> groupMap = new HashMap<String, String>();
        groupMap.put(GROUP_NAME, group.getGroupKey().getName());
        return groupMap;
    }
}
