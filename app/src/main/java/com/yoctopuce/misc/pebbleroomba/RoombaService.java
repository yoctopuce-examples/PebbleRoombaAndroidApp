package com.yoctopuce.misc.pebbleroomba;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.yoctopuce.misc.pebbleroomba.roomba.RoombaControler;

public class RoombaService extends IntentService
{


    public static final int START_LIVE_REPORT = 1;
    public static final int STOP_LIVE_REPORT = 2;
    public static final int CLEAN = 3;
    public static final int SPOT = 4;
    public static final int DOCK = 5;
    public static final int GET_CURRENT_STATE = 6;
    // input intent keys
    public static final String INTENT_KEY_COMMAND = "com.yoctopuce.roomba.COMMAND";


    public RoombaService()
    {
        super("RoombaService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        int requestType = intent.getIntExtra(INTENT_KEY_COMMAND, 0);
        Log.d("SRV", String.format("Get intent %d", requestType));
        switch (requestType) {
            case START_LIVE_REPORT:
                RoombaControler.getInstance(this).startLiveReport();
                break;
            case STOP_LIVE_REPORT:
                RoombaControler.getInstance(this).stopLiveReport();
                break;
            case CLEAN:
                RoombaControler.getInstance(this).clean();
                return;
            case SPOT:
                RoombaControler.getInstance(this).spot();
                return;
            case DOCK:
                RoombaControler.getInstance(this).dock();
                return;
            case GET_CURRENT_STATE:
                try {
                    RoombaControler.getInstance(this).getState(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            default:
                break;

        }

    }


}
