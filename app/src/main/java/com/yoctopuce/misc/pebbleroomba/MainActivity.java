package com.yoctopuce.misc.pebbleroomba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yoctopuce.misc.pebbleroomba.roomba.RoombaBroadcastReceiver;
import com.yoctopuce.misc.pebbleroomba.roomba.RoombaControler;
import com.yoctopuce.misc.pebbleroomba.roomba.RoombaSensors;


//todo: check why roomba is not waking up
//todo: periodic sate and sensor update
//todo: gess state form sensor states
//todo: free api when no more user look for broacast register
//todo: get ip from settings
//todo: install app automaticaly
//todo: nicer UI

public class MainActivity extends ActionBarActivity
{
    private TextView _messageView;
    private Button _cleanButton;
    private Button _dockButton;
    private Button _spotButton;
    private ProgressBar _progressBar;
    private Handler handler = new Handler();


    private BroadcastReceiver _roombaStateReceiver = new RoombaBroadcastReceiver()
    {
        @Override
        protected void onSensorChange(Context context, RoombaSensors sensor)
        {
        }


        @Override
        protected void onStateChange(Context context, RoombaControler.State state, String error)
        {
            setConnectedState(state, error);
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_passive);
        _messageView = (TextView) findViewById(R.id.message);
        _cleanButton = (Button) findViewById(R.id.clean_button);
        _cleanButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, RoombaService.class);
                intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.CLEAN);
                startService(intent);
            }
        });
        _spotButton = (Button) findViewById(R.id.spot_button);
        _spotButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, RoombaService.class);
                intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.SPOT);
                startService(intent);
            }
        });
        _dockButton = (Button) findViewById(R.id.dock_button);
        _dockButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, RoombaService.class);
                intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.DOCK);
                startService(intent);
            }
        });
        _progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart()
    {
        super.onStart();
        registerReceiver(_roombaStateReceiver,
                new IntentFilter(RoombaBroadcastReceiver.ACTION_NEW_STATE));
        Intent intent = new Intent(this, RoombaService.class);
        intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.START_LIVE_REPORT);
        startService(intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        handler.post(r);
    }


    @Override
    public void onStop()
    {
        Intent intent = new Intent(this, RoombaService.class);
        intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.STOP_LIVE_REPORT);
        startService(intent);
        unregisterReceiver(_roombaStateReceiver);
        super.onStop();
    }

    private void setConnectedState(RoombaControler.State state, String msg)
    {
        boolean enabled = state != RoombaControler.State.DISCONNECTED;
        _cleanButton.setEnabled(enabled);
        _dockButton.setEnabled(enabled);
        _spotButton.setEnabled(enabled);
        if (enabled) {
            switch (state) {
                case CLEANING:
                    _messageView.setText(R.string.cleaning);
                    break;
                case DOCKING:
                    _messageView.setText(R.string.docking);
                    break;
                case SPOT:
                    _messageView.setText(R.string.spotting);
                    break;
                case OFF:
                    _messageView.setText(R.string.off);
                    break;
                case DOCKED:
                    _messageView.setText(R.string.docked);
                    break;
            }
            _progressBar.setVisibility(View.GONE);
        } else {
            _progressBar.setVisibility(View.VISIBLE);
            _messageView.setText(msg);
        }
    }
    final Runnable r = new Runnable()
    {
        public void run()
        {
            Intent intent = new Intent(MainActivity.this, RoombaService.class);
            intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.GET_CURRENT_STATE);
            startService(intent);

        }
    };

}
