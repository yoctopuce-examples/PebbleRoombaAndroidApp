package com.yoctopuce.misc.pebbleroomba.pebble;

import android.content.Context;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.yoctopuce.misc.pebbleroomba.R;
import com.yoctopuce.misc.pebbleroomba.roomba.RoombaBroadcastReceiver;
import com.yoctopuce.misc.pebbleroomba.roomba.RoombaControler;
import com.yoctopuce.misc.pebbleroomba.roomba.RoombaSensors;

public class RoombaToPebbleReceiver extends RoombaBroadcastReceiver
{
    private static final int ROOMBA_MESSAGE = 1;
    private static final int ROOMBA_STATE = 2;
    private static final int ROOMBA_STATE_CLEANING = 0;
    private static final int ROOMBA_STATE_SPOTING = 1;
    private static final int ROOMBA_STATE_DOCKING = 2;
    private static final int ROOMBA_STATE_OFF = 3;
    private static final int ROOMBA_STATE_OFFLINE = 4;
    private static final int ROOMBA_STATE_DOCKED = 5;

    @Override
    protected void onSensorChange(Context context, RoombaSensors sensor)
    {

    }

    @Override
    protected void onStateChange(Context context, RoombaControler.State state, String error)
    {
        String stateStr;
        int pb_state = ROOMBA_STATE_OFF;
        // Add a key of 1, and a string value.
        switch (state) {
            case CLEANING:
                stateStr = context.getString(R.string.cleaning);
                pb_state = ROOMBA_STATE_CLEANING;
                break;
            case DOCKING:
                stateStr = context.getString(R.string.docking);
                pb_state = ROOMBA_STATE_DOCKING;
                break;
            case SPOT:
                stateStr = context.getString(R.string.spotting);
                pb_state = ROOMBA_STATE_SPOTING;
                break;
            case OFF:
                stateStr = context.getString(R.string.off);
                pb_state = ROOMBA_STATE_OFF;
                break;
            case DOCKED:
                stateStr = context.getString(R.string.docked);
                pb_state = ROOMBA_STATE_DOCKED;
                break;
            default:
            case DISCONNECTED:
                stateStr = context.getString(R.string.disconnected);
                pb_state = ROOMBA_STATE_OFFLINE;
                break;
        }
        PebbleDictionary data = new PebbleDictionary();
        data.addString(ROOMBA_MESSAGE, stateStr);
        data.addInt16(ROOMBA_STATE, (short) pb_state);
        PebbleKit.sendDataToPebble(context, PebbleToRombaReceiver.PEBBLE_APP_UUID, data);
    }
}
