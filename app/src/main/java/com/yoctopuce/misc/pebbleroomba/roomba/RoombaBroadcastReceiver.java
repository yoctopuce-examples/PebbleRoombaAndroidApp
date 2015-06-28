package com.yoctopuce.misc.pebbleroomba.roomba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class RoombaBroadcastReceiver extends BroadcastReceiver
{

    private static final String TAG = "RoombaBroadcastReceiver";
    // broadcast infos
    public static final String ACTION_NEW_STATE = "com.yoctopuce.misc.pebbleroomba.action.NEW_STATE";
    public static final String INTENT_KEY_STATE = "state";
    private static final String INTENT_KEY_SENSORS = "sensor";
    private static final String INTENT_KEY_ERROR = "error";

    @Override
    public void onReceive(Context context, Intent intent)
    {

        RoombaSensors sensor = (RoombaSensors) intent.getParcelableExtra(INTENT_KEY_SENSORS);
        if (sensor != null) {
            onSensorChange(context, sensor);
            return;
        }
        RoombaControler.State state = (RoombaControler.State) intent.getSerializableExtra(INTENT_KEY_STATE);
        String error = intent.getStringExtra(INTENT_KEY_ERROR);
        onStateChange(context, state, error);
    }

    abstract protected void onSensorChange(Context context, RoombaSensors sensor);

    abstract protected void onStateChange(Context context, RoombaControler.State state, String error);


    // helper method to generate broadcast intent
    public static void sendStateChange(Context ctx, RoombaControler.State state)
    {
        Intent intentUpdate = new Intent(ACTION_NEW_STATE);
        intentUpdate.putExtra(INTENT_KEY_STATE, state);
        ctx.sendBroadcast(intentUpdate);
    }

    public static void sendStateChange(Context ctx, RoombaControler.State state, String error)
    {
        Intent intentUpdate = new Intent(ACTION_NEW_STATE);
        intentUpdate.putExtra(INTENT_KEY_STATE, state);
        intentUpdate.putExtra(INTENT_KEY_ERROR, error);
        ctx.sendBroadcast(intentUpdate);
    }
}