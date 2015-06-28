package com.yoctopuce.misc.pebbleroomba.pebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.yoctopuce.misc.pebbleroomba.RoombaService;

import org.json.JSONException;

import java.util.UUID;

public class PebbleToRombaReceiver extends BroadcastReceiver
{
    private static final String TAG = "PebbleToRombaReceiver";
    public final static UUID PEBBLE_APP_UUID = UUID.fromString("3173b512-fe9c-4be9-98c6-db18daee5076");

    private final static int ROOMBA_CMD = 0;
    private final static int CLEAN = 0;
    private final static int SPOT = 1;
    private final static int DOCK = 2;
    private final static int GET_STATE = 3;

    @Override
    public void onReceive(Context context, Intent intent)
    {

        final UUID receivedUuid = (UUID) intent.getSerializableExtra(Constants.APP_UUID);

        // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts
        // containing their UUID

        if (!PEBBLE_APP_UUID.equals(receivedUuid)) {
            return;
        }

        final int transactionId = intent.getIntExtra(Constants.TRANSACTION_ID, -1);
        final String jsonData = intent.getStringExtra(Constants.MSG_DATA);
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }
        try {
            final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
            receiveData(context, transactionId, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void receiveData(Context context, int transactionId, PebbleDictionary data)
    {
        int buttonIndex = data.getInteger(ROOMBA_CMD).intValue();
        //Log.d("PBL", data.toJsonString());
        //Log.d("PBL", String.format("cmd = %d",buttonIndex));
        Intent intent = new Intent(context, RoombaService.class);
        switch (buttonIndex) {
            case CLEAN:
                intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.CLEAN);
                break;
            case SPOT:
                intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.SPOT);
                break;
            case DOCK:
                intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.DOCK);
                break;
            case GET_STATE:
                intent.putExtra(RoombaService.INTENT_KEY_COMMAND, RoombaService.GET_CURRENT_STATE);
                break;
            default:
                return;
        }
        context.startService(intent);
    }
}
