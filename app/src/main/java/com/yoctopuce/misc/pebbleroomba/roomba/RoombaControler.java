package com.yoctopuce.misc.pebbleroomba.roomba;

import android.content.Context;
import android.util.Log;

import com.yoctopuce.YoctoAPI.YAPI_Exception;

import java.util.ArrayList;
import java.util.List;

public class RoombaControler
{
    private static final String ROOMBA_IP = "192.168.0.28";
    private static final String TAG = "RoombaControler";
    // field
    private static RoombaControler __instance;
    private final Roomba _roomba;
    private final Context _appContext;
    private State _state;
    private int _liveReportUsers;
    private long _lastRefresh = 0;


    private RoombaControler(Context appContext)
    {
        _roomba = new Roomba(ROOMBA_IP);
        _state = State.DISCONNECTED;
        _liveReportUsers = 0;
        _appContext = appContext;
    }

    public synchronized static RoombaControler getInstance(Context ctx)
    {
        if (__instance == null) {
            __instance = new RoombaControler(ctx.getApplicationContext());
        }
        return __instance;
    }


    public void startLiveReport()
    {
        if (_liveReportUsers == 0) {
            Log.d(TAG, "Start Periodic handling");
        }
        _liveReportUsers++;
    }

    public void stopLiveReport()
    {
        _liveReportUsers--;
        if (_liveReportUsers == 0) {
            Log.d(TAG, "Stop Periodic handling");
        }

    }

    public State guessState(boolean forceConnection) throws InterruptedException
    {
        try {

            if (_state == State.DISCONNECTED) {
                if (forceConnection)
                    _roomba.connect();

                else
                    return State.DISCONNECTED;
            }
            _roomba.clearCommandLog();
            RoombaSensors roombaSensors = _roomba.GetSensor(RoombaSensors.SENSOR_ID.SENSOR_GROUP_100);
            ArrayList<String> commandLog = _roomba.getCommandLog();
            for (String log : commandLog) {
                Log.d("rlog", log);
            }
            int charging = roombaSensors.getChargerAvailable();
            int mainBrushCurrent = Math.abs(roombaSensors.getMainBrushCurrent());
            boolean statis = roombaSensors.isStasis();
            Log.d("state", String.format("statis=%s mainbrush=%d charging=%d ",
                    statis ? "yes" : "no", mainBrushCurrent, charging));

            if (mainBrushCurrent < 5) {
                if (charging > 0) {
                    return State.DOCKED;
                } else {
                    return State.OFF;
                }
            } else {
                switch (_state) {
                    case CLEANING:
                    case SPOT:
                    case DOCKING:
                        return _state;
                    case DISCONNECTED:
                    case OFF:
                    case DOCKED:
                    default:
                        return State.CLEANING;
                }
            }
        } catch (YAPI_Exception e) {
            e.printStackTrace();
            return State.DISCONNECTED;
        } catch (RoombaSensors.RoombaException e) {
            e.printStackTrace();
            return State.DISCONNECTED;
        }
    }


    public void clean()
    {
        try {
            if (_state == State.DISCONNECTED) {
                _roomba.connect();
                _state = State.OFF;
            }
            if (_state == State.CLEANING) {
                _roomba.safe();
                _state = State.OFF;
            } else {
                _roomba.safe();
                _roomba.stdClean();
                _state = State.CLEANING;
            }
            RoombaBroadcastReceiver.sendStateChange(_appContext, _state);
        } catch (YAPI_Exception e) {
            e.printStackTrace();
            setIOError(e.getLocalizedMessage());
            _state = State.DISCONNECTED;
        }
    }

    private void setIOError(String localizedMessage)
    {
        Log.e(TAG, localizedMessage);
        _state = State.DISCONNECTED;
        RoombaBroadcastReceiver.sendStateChange(_appContext, _state, localizedMessage);
    }

    public void spot()
    {
        try {
            if (_state == State.DISCONNECTED) {
                _roomba.connect();
                _state = State.OFF;

            }
            if (_state == State.SPOT) {
                _roomba.safe();
                _state = State.OFF;
            } else {
                _roomba.safe();
                _roomba.spotClean();
                _state = State.SPOT;
            }
            RoombaBroadcastReceiver.sendStateChange(_appContext, _state);
        } catch (YAPI_Exception e) {
            e.printStackTrace();
            setIOError(e.getLocalizedMessage());
            _state = State.DISCONNECTED;
        }

    }

    public void dock()
    {
        try {
            if (_state == State.DISCONNECTED) {
                _roomba.connect();
                _state = State.OFF;
            }
            if (_state == State.DOCKING) {
                _roomba.safe();
                _state = State.OFF;
            } else {
                _roomba.safe();
                _roomba.dock();
                _state = State.DOCKING;
            }
            RoombaBroadcastReceiver.sendStateChange(_appContext, _state);
        } catch (YAPI_Exception e) {
            e.printStackTrace();
            setIOError(e.getLocalizedMessage());
        }
    }


    public void getState(boolean realState) throws InterruptedException
    {
        try {
            State org_state = _state;
            long now = System.currentTimeMillis();
            if (realState && (now - _lastRefresh) > 10000) {
                _state = guessState(true);
                _lastRefresh = now;
            }
            if (_state == State.DISCONNECTED) {
                _roomba.connect();
                _state = State.OFF;
            }
            Log.d(TAG, String.format("Guess State (%s) %s->%s", realState ? "real" : "", org_state, _state));
            RoombaBroadcastReceiver.sendStateChange(_appContext, _state);
        } catch (YAPI_Exception e) {
            e.printStackTrace();
            setIOError(e.getLocalizedMessage());
        }

    }


    public enum State
    {
        DISCONNECTED, OFF, CLEANING, SPOT, DOCKING, DOCKED
    }

    public void run()
    {
        try {
            _roomba.connect();
            _state = State.OFF;
            while (true) {
                RoombaSensors roombaSensors = _roomba.GetSensor(RoombaSensors.SENSOR_ID.SENSOR_GROUP_6);
                System.out.println(roombaSensors.toString());
                Thread.sleep(1000);
            }
        } catch (YAPI_Exception | InterruptedException e) {
            e.printStackTrace();
        } finally {
            List<String> log = _roomba.getCommandLog();
            for (String line : log) {
                System.out.print(line + "\n");
            }
            _roomba.disconnect();
        }
    }


}
