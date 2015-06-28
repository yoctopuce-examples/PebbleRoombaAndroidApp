package com.yoctopuce.misc.pebbleroomba.roomba;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YSerialPort;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Roomba
{
    private YSerialPort _serial_port;
    private String _url;
    private boolean _connected = false;
    private RoombaSensors.OIMode _expectedMode;
    final ArrayList<String> _commandLog = new ArrayList<String>(5);

    public Roomba(String url)
    {

        _url = url;
    }

    public void connect() throws YAPI_Exception
    {
        YAPI.RegisterHub(_url);
        _serial_port = YSerialPort.FirstSerialPort();
        if (_serial_port == null)
            throw new YAPI_Exception(YAPI.DEVICE_NOT_FOUND, "No module found on address (check USB cable)");

        _serial_port.set_voltageLevel(YSerialPort.VOLTAGELEVEL_TTL5V);
        // setup the serial parameter with the Roomba
        // serie 600 and following communicate at 115200 bauds
        _serial_port.set_serialMode("115200,8N1");
        // let the serial port wait 20ms between each writes
        _serial_port.set_protocol("Frame:15ms:10ms");
        // clean old data
        _serial_port.reset();
        startOI();
    }

    public void clearCommandLog()
    {
        _commandLog.clear();
    }

    public ArrayList<String> getCommandLog()
    {
        return _commandLog;
    }

    public void disconnect()
    {
        try {
            stopOI();
        } catch (YAPI_Exception e) {
            e.printStackTrace();
        }
        _connected = false;
        YAPI.FreeAPI();
    }

    private byte[] _sendCmd(ArrayList<Integer> cmd, int nbytes) throws YAPI_Exception, InterruptedException
    {
        if (nbytes > 0) {
            _serial_port.reset();
        }
        _sendCmd(cmd);
        if (nbytes > 0) {
            int avail = 0;
            long timeout = YAPI.GetTickCount() + 2000;
            while (avail < nbytes && timeout > YAPI.GetTickCount()) {
                avail = _serial_port.read_avail();
                if (avail < nbytes) {
                    Thread.sleep(10);
                }
            }
            ArrayList<Integer> readed = _serial_port.readArray(nbytes);
            byte[] res = new byte[readed.size()];
            String logline = "<";

            for (int i = 0; i < readed.size(); i++) {
                int b = readed.get(i) & 0xff;
                logline += String.format("%02x", b);
                res[i] = (byte) (b);
            }
            _commandLog.add(logline);
            return res;
        } else {
            return new byte[0];
        }
    }

    private void _sendCmd(ArrayList<Integer> cmd) throws YAPI_Exception
    {

        _serial_port.writeArray(cmd);
        String logline = ">";
        for (int i = 0; i < cmd.size(); i++) {
            logline += String.format("%02x", cmd.get(i));
        }
        _commandLog.add(logline);

    }

    private void _sendCmd(int cmd) throws YAPI_Exception
    {

        _serial_port.writeByte(cmd);
        _commandLog.add(String.format(">%02x", cmd));

    }

    private void startOI() throws YAPI_Exception
    {
        _sendCmd(128);
        _expectedMode = RoombaSensors.OIMode.PASSIVE;
    }

    private void stopOI() throws YAPI_Exception
    {
        _sendCmd(173);
        _expectedMode = RoombaSensors.OIMode.OFF;
    }


    public void setMode(RoombaSensors.OIMode mode) throws YAPI_Exception
    {
        switch (mode) {
            case OFF:
                stopOI();
                break;
            case PASSIVE:
                startOI();
                break;
            case SAFE:
                safe();
                break;
            case FULL:
                full();
                break;
        }
    }

    public void safe() throws YAPI_Exception
    {
        _sendCmd(131);
        _expectedMode = RoombaSensors.OIMode.SAFE;
    }

    public RoombaSensors.OIMode getExpectedMode()
    {
        return _expectedMode;
    }

    public void full() throws YAPI_Exception
    {
        _sendCmd(132);
        _expectedMode = RoombaSensors.OIMode.FULL;

    }


    public void power() throws YAPI_Exception
    {
        _sendCmd(133);
        _expectedMode = RoombaSensors.OIMode.PASSIVE;
    }

    public void spotClean() throws YAPI_Exception
    {
        _sendCmd(134);
        _expectedMode = RoombaSensors.OIMode.PASSIVE;

    }

    public void stdClean() throws YAPI_Exception
    {
        _sendCmd(135);
        _expectedMode = RoombaSensors.OIMode.PASSIVE;

    }

    public void maxClean() throws YAPI_Exception
    {
        _sendCmd(136);
        _expectedMode = RoombaSensors.OIMode.PASSIVE;

    }

    public void dock() throws YAPI_Exception
    {
        _sendCmd(143);
        _expectedMode = RoombaSensors.OIMode.PASSIVE;
    }


    public void Drive(int velocity, int radius) throws YAPI_Exception
    {
        if (velocity > 500)
            velocity = 500;
        if (velocity < -500)
            velocity = -500;
        if (radius > 2000)
            radius = 2000;
        if (radius < -2000)
            radius = -2000;

        ArrayList<Integer> cmd = new ArrayList<Integer>();
        cmd.add(137);
        cmd.add(((int) velocity) >> 8);
        cmd.add(((int) velocity) & 0xff);
        cmd.add(((int) radius) >> 8);
        cmd.add(((int) radius) & 0xff);
        _sendCmd(cmd);
    }

    public void Motors(boolean main_brush, boolean vacum, boolean side_brush) throws YAPI_Exception
    {
        int flags = 0;
        if (main_brush)
            flags |= 4;
        if (vacum)
            flags |= 2;
        if (side_brush)
            flags |= 1;
        ArrayList<Integer> cmd = new ArrayList<Integer>();
        cmd.add(138);
        cmd.add(flags);
        _sendCmd(cmd);
    }

    public void Leds(int power_color, int power_intensity, boolean debris_led, boolean spot_led, boolean dock_led, boolean check_led) throws YAPI_Exception
    {
        ArrayList<Integer> cmd = new ArrayList<Integer>();

        cmd.add(139);
        int flags = 0;
        if (debris_led)
            flags += 1;
        if (spot_led)
            flags += 2;
        if (dock_led)
            flags += 4;
        if (check_led)
            flags += 8;
        cmd.add(flags);
        cmd.add(power_color & 0xff);
        cmd.add(power_intensity & 0xff);
        _sendCmd(cmd);
    }

    public void Song(int songno, ArrayList<Note> notes) throws YAPI_Exception
    {
        ArrayList<Integer> cmd = new ArrayList<Integer>();


        cmd.add(140);
        cmd.add(songno);
        int len = notes.size();
        if (len > 16) {
            len = 16;
        }
        cmd.add(len);
        for (int i = 0; i < len; i++) {
            cmd.add(notes.get(i).frequency);
            cmd.add(notes.get(i).duration);
        }
        _sendCmd(cmd);
    }

    public void Play(int songno) throws YAPI_Exception
    {
        ArrayList<Integer> cmd = new ArrayList<Integer>();
        cmd.add(141);
        cmd.add(songno);
        _sendCmd(cmd);
    }

    public void LedASCII(String message) throws YAPI_Exception
    {
        ArrayList<Integer> cmd = new ArrayList<Integer>();

        cmd.add(164);
        cmd.add(message.codePointAt(0));
        cmd.add(message.codePointAt(1));
        cmd.add(message.codePointAt(2));
        cmd.add(message.codePointAt(3));
        _sendCmd(cmd);
    }


    public void SetTime(Date time) throws YAPI_Exception
    {
        Calendar c = Calendar.getInstance();
        c.setTime(time);
        ArrayList<Integer> cmd = new ArrayList<Integer>();
        cmd.add(168);
        cmd.add(c.get(Calendar.DAY_OF_WEEK));
        cmd.add(c.get(Calendar.HOUR_OF_DAY));
        cmd.add(c.get(Calendar.MINUTE));
        _sendCmd(cmd);
    }


    public RoombaSensors GetSensor(RoombaSensors.SENSOR_ID sensor_id) throws YAPI_Exception, InterruptedException
    {
        ArrayList<Integer> cmd = new ArrayList<Integer>();
        cmd.add(142);
        cmd.add(sensor_id.getNumVal());
        byte[] raw = _sendCmd(cmd, sensor_id.getSize());
        return new RoombaSensors(sensor_id, raw);
    }


    public class Note
    {
        public int frequency;
        public int duration;
    }
}
