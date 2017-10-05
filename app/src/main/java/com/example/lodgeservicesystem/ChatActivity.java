package com.example.lodgeservicesystem;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends ActionBarActivity {

    private Messenger service = null;
    private final Messenger serviceHandler = new Messenger(new ServiceHandler());
    private IntentFilter intentFilter = null;
    private PushReceiver pushReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.lodgeservicesystem.PushReceived");
        pushReceiver = new PushReceiver();
        registerReceiver(pushReceiver, intentFilter, null, null);

        startService(new Intent(this, ChatService.class));
        addSubscribeButtonListener();
        addPublishButtonListener();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        bindService(new Intent(this, ChatService.class), serviceConnection, 0);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(pushReceiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(pushReceiver);
    }

    public class PushReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent i)
        {
            String topic = i.getStringExtra(ChatService.TOPIC);
            String message = i.getStringExtra(ChatService.MESSAGE);
            Toast.makeText(context, "Push message received - " + topic + ":" + message, Toast.LENGTH_LONG).show();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder)
        {
            service = new Messenger(binder);
            Bundle data = new Bundle();
            //data.putSerializable(ChatService.CLASSNAME, MainActivity.class);
            data.putCharSequence(ChatService.INTENTNAME, "com.example.lodgeservicesystem.PushReceived");
            Message msg = Message.obtain(null, ChatService.REGISTER);
            msg.setData(data);
            msg.replyTo = serviceHandler;
            try
            {
                service.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
        }
    };

    private void addSubscribeButtonListener()
    {
        Button subscribeButton = (Button) findViewById(R.id.chatSubscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener()
        {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onClick(View arg0)
            {
                TextView result = (TextView) findViewById(R.id.chatTextResultStatus);
                EditText t = (EditText) findViewById(R.id.chatTopics);
                String topic = t.getText().toString().trim();
                inputMethodManager.hideSoftInputFromWindow(result.getWindowToken(), 0);

                if (topic != null && topic.isEmpty() == false)
                {
                    result.setText("");
                    Bundle data = new Bundle();
                    data.putCharSequence(ChatService.TOPIC, topic);
                    Message msg = Message.obtain(null, ChatService.SUBSCRIBE);
                    msg.setData(data);
                    msg.replyTo = serviceHandler;
                    try
                    {
                        service.send(msg);
                    }
                    catch (RemoteException e)
                    {
                        e.printStackTrace();
                        result.setText("Subscribe failed with exception:" + e.getMessage());
                    }
                }
                else
                {
                    result.setText("Topic required.");
                }
            }
        });
    }

    private void addPublishButtonListener()
    {
        Button publishButton = (Button) findViewById(R.id.chatSendButton);
        publishButton.setOnClickListener(new View.OnClickListener()
        {

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            @Override
            public void onClick(View arg0)
            {
                EditText t = (EditText) findViewById(R.id.chatTopics);
                EditText m = (EditText) findViewById(R.id.chatEditMessage);
                TextView result = (TextView) findViewById(R.id.chatTextResultStatus);
                inputMethodManager.hideSoftInputFromWindow(result.getWindowToken(), 0);

                String topic = t.getText().toString().trim();
                String message = m.getText().toString().trim();

                if (topic != null && topic.isEmpty() == false && message != null && message.isEmpty() == false)
                {
                    result.setText("");
                    Bundle data = new Bundle();
                    data.putCharSequence(ChatService.TOPIC, topic);
                    data.putCharSequence(ChatService.MESSAGE, message);
                    Message msg = Message.obtain(null, ChatService.PUBLISH);
                    msg.setData(data);
                    msg.replyTo = serviceHandler;
                    try
                    {
                        service.send(msg);
                    }
                    catch (RemoteException e)
                    {
                        e.printStackTrace();
                        result.setText("Publish failed with exception:" + e.getMessage());
                    }
                }
                else
                {
                    result.setText("Topic and message required.");
                }
            }
        });
    }

    class ServiceHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case ChatService.SUBSCRIBE: 	break;
                case ChatService.PUBLISH:		break;
                case ChatService.REGISTER:		break;
                default:
                    super.handleMessage(msg);
                    return;
            }

            Bundle b = msg.getData();
            if (b != null)
            {
                TextView result = (TextView) findViewById(R.id.chatTextResultStatus);
                Boolean status = b.getBoolean(ChatService.STATUS);
                if (status == false)
                {
                    result.setText("Fail");
                }
                else
                {
                    result.setText("Success");
                }
            }
        }
    }
}
