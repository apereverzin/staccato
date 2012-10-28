package com.creek.staccato.android.activity.message;

import com.creek.staccato.android.activity.R;
import com.creek.staccato.android.activity.util.ActivityUtil;
import com.creek.staccato.domain.message.InformationMessage;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class MessageViewActivity extends Activity {
    private TextView titleView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.message_view);
        titleView = (TextView) findViewById(R.id.message_title);
        textView = (TextView) findViewById(R.id.message_text);

        Bundle extras = getIntent().getExtras();

        final InformationMessage message = (InformationMessage) extras.get(MessagesListActivity.MESSAGE);
        titleView.setText(message.getTitle());
        textView.setText(message.getText());

        StringBuilder title = ActivityUtil.buildActivityTitle(this, R.string.groups_activity_name, R.string.message_activity_name);
        setTitle(title);
    }
}
