package com.creek.staccato.android.activity.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.group.GroupsListActivity;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.android.cache.CacheManager;
import com.creek.staccato.domain.group.Group;
import com.creek.staccato.domain.message.InformationMessage;
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
 * List of all messages.
 * 
 * @author Andrey Pereverzin
 */
public class MessagesListActivity extends ListActivity {
    private static final String TAG = MessagesListActivity.class.getSimpleName();

    private static final int VIEW_MESSAGE_MENU_ITEM = Menu.FIRST;

    private static final String GROUP_NAME = "group_name";

    private List<Map<String, String>> messageList;
    private List<InformationMessage> messageDataList;
    private SimpleAdapter groupsListAdapter;
    
    public static final String MESSAGE = "MESSAGE";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.messages_list);

        Bundle extras = getIntent().getExtras();
        Group group = extras == null ? null : (Group) extras.get(GroupsListActivity.GROUP);

        try {
            messageList = new LinkedList<Map<String, String>>();
            if(group == null) {
                messageDataList = new ArrayList<InformationMessage>(CacheManager.getInstance().readMessagesFromInformationMessagesDir());
            } else {
                messageDataList = new ArrayList<InformationMessage>(CacheManager.getInstance().readMessagesFromInformationMessagesDir());
                //messageDataList = new ArrayList<InformationMessage>(ApplManager.getInstance().getMessageService().getMessagesForGroup(group);
            }

            for (InformationMessage message : messageDataList) {
                Map<String, String> groupMap = createMessageMapForList(message);
                messageList.add(groupMap);
            }
            groupsListAdapter = new SimpleAdapter(getApplicationContext(), messageList, R.layout.group_row, new String[] { GROUP_NAME }, new int[] { R.id.group_name });
            setListAdapter(groupsListAdapter);

            registerForContextMenu(getListView());
        } catch (TransformException ex) {
            ActivityUtil.showException(this, ex);
        }

        StringBuilder title = ActivityUtil.buildActivityTitle(this, R.string.groups_activity_name, R.string.messages_activity_name);
        setTitle(title);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu()");
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, VIEW_MESSAGE_MENU_ITEM, 0, R.string.view);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        final InformationMessage messageSelected = messageDataList.get((int) info.id);
        switch (item.getItemId()) {
        case VIEW_MESSAGE_MENU_ITEM:
            Log.d(TAG, "VIEW_MESSAGE_MENU_ITEM");
            Intent viewMessageIntent = new Intent(MessagesListActivity.this, MessageViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MESSAGE, messageSelected);
            viewMessageIntent.putExtras(bundle);
            startActivity(viewMessageIntent);
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

    private Map<String, String> createMessageMapForList(InformationMessage message) {
        Map<String, String> groupMap = new HashMap<String, String>();
        groupMap.put(GROUP_NAME, message.getTitle());
        return groupMap;
    }
}
