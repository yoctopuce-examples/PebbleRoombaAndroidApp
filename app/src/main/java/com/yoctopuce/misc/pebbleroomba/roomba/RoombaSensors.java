package com.yoctopuce.misc.pebbleroomba.roomba;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class RoombaSensors
{

    public class RoombaException extends Exception
    {

        public RoombaException(String s)
        {
            super(s);
        }
    }

    public enum ChargingState
    {
        NOT_CHARGING,
        RECONDITIONING_CHARGING,
        FULL_CHARGING,
        TRICKLE_CHARGING,
        WAITING,
        CHARGING_FAULT_CONDITION
    }

    public enum OIMode
    {
        OFF, PASSIVE, SAFE, OIMode, FULL
    }

    private final SENSOR_ID _sensor_id;
    private final byte[] _raw;

    public enum SENSOR_ID
    {
        SENSOR_GROUP_0(0, 7, 26, 26),
        SENSOR_GROUP_1(1, 7, 16, 10),
        SENSOR_GROUP_2(2, 17, 20, 6),
        SENSOR_GROUP_3(3, 21, 26, 10),
        SENSOR_GROUP_4(4, 27, 34, 14),
        SENSOR_GROUP_5(5, 35, 42, 12),
        SENSOR_GROUP_6(6, 7, 42, 52),
        BUMPS_WHEELDROPS(7, 1),
        WALL(8, 1),
        CLIFF_LEFT(9, 1),
        CLIFF_FRONT_LEFT(10, 1),
        CLIFF_FRONT_RIGHT(11, 1),
        CLIFF_RIGHT(12, 1),
        VIRTUAL_WALL(13, 1),
        OVERCURRENTS(14, 1),
        DIRT_DETECT(15, 1),
        UNUSED(16, 1),
        IR_CHAR_OMNI(17, 1),
        BUTTONS(18, 1),
        DISTANCE(19, 2, true),
        ANGLE(20, 2, true),
        CHARGING_STATE(21, 1),
        VOLTAGE(22, 2),
        CURRENT(23, 2, true),
        TEMPERATURE(24, 1, true),
        BATTERY_CHARGE(25, 2),
        BATTERY_CAPACITY(26, 2),
        WALL_SIGNAL(27, 2),
        CLIFF_LEFT_SIGNAL(28, 2),
        CLIFF_FRONT_LEFT_SIGNAL(29, 2),
        CLIFF_FRONT_RIGHT_SIGNAL(30, 2),
        CLIFF_RIGHT_SIGNAL(31, 2),
        UNUSED_2(32, 1),
        UNUSED_3(33, 2),
        CHARGER_AVAILABLE(34, 1),
        OPEN_INTERFACE_MODE(35, 1),
        SONG_NUMBER(36, 1),
        SONG_PLAYING(37, 1),
        OI_STREAM_NUM_PACKETS(38, 1),
        VELOCITY(39, 2, true),
        RADIUS(40, 2, true),
        VELOCITY_RIGHT(41, 2, true),
        VELOCITY_LEFT(42, 2, true),
        ENCODER_COUNTS_LEFT(43, 2),
        ENCODER_COUNTS_RIGHT(44, 2),
        LIGHT_BUMPER(45, 1),
        LIGHT_BUMP_LEFT(46, 2),
        LIGHT_BUMP_FRONT_LEFT(47, 2),
        LIGHT_BUMP_CENTER_LEFT(48, 2),
        LIGHT_BUMP_CENTER_RIGHT(49, 2),
        LIGHT_BUMP_FRONT_RIGHT(50, 2),
        LIGHT_BUMP_RIGHT(51, 2),
        IR_OPCODE_LEFT(52, 1),
        IR_OPCODE_RIGHT(53, 1),
        LEFT_MOTOR_CURRENT(54, 2, true),
        RIGHT_MOTOR_CURRENT(55, 2, true),
        MAIN_BRUSH_CURRENT(56, 2, true),
        SIDE_BRUSH_CURRENT(57, 2, true),
        STASIS(58, 1),
        SENSOR_GROUP_100(100, 7, 58, 80),
        SENSOR_GROUP_101(101, 43, 58, 28),
        SENSOR_GROUP_106(106, 46, 51, 12),
        SENSOR_GROUP_107(107, 54, 58, 9);

        private int numVal;
        private int startOfs;
        private int stopOfs;
        private int size;
        private boolean signed;


        SENSOR_ID(int numVal, int size)
        {
            this.numVal = numVal;
            this.startOfs = numVal;
            this.stopOfs = numVal;
            this.size = size;
            this.signed = false;
        }

        SENSOR_ID(int numVal, int size, boolean signed)
        {
            this.numVal = numVal;
            this.startOfs = numVal;
            this.stopOfs = numVal;
            this.size = size;
            this.signed = signed;
        }

        SENSOR_ID(int numVal, int startOfs, int stopOfs, int size)
        {
            this.numVal = numVal;
            this.startOfs = startOfs;
            this.stopOfs = stopOfs;
            this.size = size;
            this.signed = false;
        }

        public SENSOR_ID[] getValues()
        {
            return values();
        }


        public int getNumVal()
        {
            return numVal;
        }

        public int getSize()
        {
            return size;
        }

        public boolean isSigned()
        {
            return signed;
        }

        public int getStartOfs()
        {
            return startOfs;
        }
    }

    public RoombaSensors(SENSOR_ID sensor_id, byte[] raw)
    {
        _sensor_id = sensor_id;
        _raw = raw;

    }


    private int getRawBytes(SENSOR_ID id) throws RoombaException
    {
        if (id.getNumVal() < _sensor_id.startOfs || id.getNumVal() > _sensor_id.stopOfs) {
            throw new RoombaException("Not supported");
        }
        int ofs = 0;
        int i;
        SENSOR_ID[] values = SENSOR_ID.values();
        for (i = 7; i < values.length; i++) {
            if (values[i].getNumVal() == id.getNumVal()) {
                break;
            }
            ofs += values[i].getSize();
        }

        //Log.d("RSENS", String.format("getRaw for %s (%d+%d in %d/%d)", id.toString(), id.getStartOfs(), id.getSize(),
        //        ofs, _raw.length));
        if (id.isSigned()) {
            if (id.getSize() == 1) {
                return _raw[ofs];
            } else if (id.getSize() == 2) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(_raw);
                return byteBuffer.getShort(ofs);
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(_raw);
                return byteBuffer.getInt(ofs);
            }
        } else {
            int res = 0;
            for (i = 0; i < id.getSize(); i++) {
                res <<= 8;
                res += _raw[ofs++] & 0xff;
            }
            return res;
        }
    }

    public int getBumpAndWheelDrops() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.BUMPS_WHEELDROPS) & 0xf;
    }

    public boolean getWall() throws RoombaException
    {
        return (getRawBytes(SENSOR_ID.WALL) & 1) == 1;
    }

    public boolean getCliff_left() throws RoombaException
    {
        return (getRawBytes(SENSOR_ID.CLIFF_LEFT) & 1) == 1;
    }

    public boolean getCliff_front_left() throws RoombaException
    {

        return (getRawBytes(SENSOR_ID.CLIFF_FRONT_LEFT) & 1) == 1;
    }

    public boolean getCliff_front_right() throws RoombaException
    {

        return (getRawBytes(SENSOR_ID.CLIFF_FRONT_RIGHT) & 1) == 1;
    }

    public boolean getCliff_right() throws RoombaException
    {

        return (getRawBytes(SENSOR_ID.CLIFF_RIGHT) & 1) == 1;
    }

    public boolean getVirtual_wall() throws RoombaException
    {
        return (getRawBytes(SENSOR_ID.VIRTUAL_WALL) & 1) == 1;
    }

    public int getOvercurrents() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.OVERCURRENTS) & 0xD;
    }

    public int getDirtDetect() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.DIRT_DETECT);
    }

    public int getUnused() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.UNUSED);
    }

    public int getIrCharOmni() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.IR_CHAR_OMNI);
    }

    public int getButtons() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.BUTTONS);
    }

    public int getDistance() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.DISTANCE);
    }

    public int getAngle() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.ANGLE);
    }

    public ChargingState getChargingState() throws RoombaException
    {
        int state = getRawBytes(SENSOR_ID.CHARGING_STATE);
        switch (state) {
            case 0:
                return ChargingState.NOT_CHARGING;
            case 1:
                return ChargingState.RECONDITIONING_CHARGING;
            case 2:
                return ChargingState.FULL_CHARGING;
            case 3:
                return ChargingState.TRICKLE_CHARGING;
            case 4:
                return ChargingState.WAITING;
            case 5:
                return ChargingState.CHARGING_FAULT_CONDITION;
            default:
                throw new RoombaException("invalid charging state");
        }
    }

    public int getVoltage() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.VOLTAGE);
    }

    public int getCurrent() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.CURRENT);
    }

    public int getTemperature() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.TEMPERATURE);
    }

    public int getBatteryCharge() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.BATTERY_CHARGE);
    }

    public int getBatteryCapacity() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.BATTERY_CAPACITY);
    }

    public int getWallSignal() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.WALL_SIGNAL);
    }

    public int getCliffLeftSignal() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.CLIFF_LEFT_SIGNAL);
    }

    public int getCliffFrontLeftSignal() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.CLIFF_FRONT_LEFT_SIGNAL);
    }

    public int getCliffFrontRightSignal() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.CLIFF_FRONT_RIGHT_SIGNAL);
    }

    public int getCliffRightSignal() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.CLIFF_RIGHT_SIGNAL);
    }

    public int getUnused_2() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.UNUSED_2);
    }

    public int getUnused_3() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.UNUSED_3);
    }

    public int getChargerAvailable() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.CHARGER_AVAILABLE) & 0x3;
    }

    public OIMode getOpenInterfaceMode() throws RoombaException
    {
        int rawBytes = getRawBytes(SENSOR_ID.OPEN_INTERFACE_MODE);
        switch (rawBytes) {
            default:
            case 0:
                return OIMode.OFF;
            case 1:
                return OIMode.PASSIVE;
            case 2:
                return OIMode.SAFE;
            case 3:
                return OIMode.FULL;
        }
    }

    public int getSongNumber() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.SONG_NUMBER) & 0xf;
    }

    public boolean getSongPlaying() throws RoombaException
    {
        return (getRawBytes(SENSOR_ID.SONG_PLAYING) & 1) == 0;
    }

    public int getStreamNbPackets() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.OI_STREAM_NUM_PACKETS);
    }

    public int getVelocity() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.VELOCITY);
    }

    public int getRadius() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.RADIUS);
    }

    public int getVelocityRight() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.VELOCITY_RIGHT);
    }

    public int getVelocityLeft() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.VELOCITY_LEFT);
    }

    public int getEncoderCountsLeft() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.ENCODER_COUNTS_LEFT);
    }

    public int getEncoderCountsRight() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.ENCODER_COUNTS_RIGHT);
    }

    public int getLightBumper() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LIGHT_BUMPER);
    }

    public int getLightBumpLeft() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LIGHT_BUMP_LEFT);
    }

    public int getLightBumpFrontLeft() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LIGHT_BUMP_FRONT_LEFT);
    }

    public int getLightBumpCenterLeft() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LIGHT_BUMP_CENTER_LEFT);
    }

    public int getLightBumpCenterRight() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LIGHT_BUMP_CENTER_RIGHT);
    }

    public int getLightBumpFrontRight() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LIGHT_BUMP_FRONT_RIGHT);
    }

    public int getLightBumpRight() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LIGHT_BUMP_RIGHT);
    }

    public int getIrCharLeft() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.IR_OPCODE_LEFT);
    }

    public int getIrCharRght() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.IR_OPCODE_RIGHT);
    }

    public int getLeftMotorCurrent() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.LEFT_MOTOR_CURRENT);
    }

    public int getRightMotorCurrent() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.RIGHT_MOTOR_CURRENT);
    }

    public int getMainBrushCurrent() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.MAIN_BRUSH_CURRENT);
    }

    public int getSideBrushCurrent() throws RoombaException
    {
        return getRawBytes(SENSOR_ID.SIDE_BRUSH_CURRENT);
    }

    public boolean isStasis() throws RoombaException
    {
        return (getRawBytes(SENSOR_ID.STASIS) & 1) == 0;
    }


}
