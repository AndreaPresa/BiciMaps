package com.example.michel.bicimaps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.util.UUID;

public class BluetoothDeviceManager {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BroadcastReceiver mBluetoothBroadcastReceiver;
    private UUID mUUID;
    private Handler mBluetoothHandler;

    public BluetoothDeviceManager () {

    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

    public Handler getmBluetoothHandler() {
        return mBluetoothHandler;
    }

    public UUID getmUUID() {
        return mUUID;
    }

    public BroadcastReceiver getmBluetoothBroadcastReceiver(){
        return mBluetoothBroadcastReceiver;
    }

    public void setmBluetoothAdapter (BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter=mBluetoothAdapter;
    }

    public void setmBluetoothBroadcastReceiver(BroadcastReceiver mBluetoothBroadcastReceiver) {
        this.mBluetoothBroadcastReceiver = mBluetoothBroadcastReceiver;
    }

    public void setmBluetoothDevice(BluetoothDevice mBluetoothDevice) {
        this.mBluetoothDevice = mBluetoothDevice;
    }

    public void setmBluetoothHandler(Handler mBluetoothHandler) {
        this.mBluetoothHandler = mBluetoothHandler;
    }

    public void setmUUID(UUID mUUID) {
        this.mUUID = mUUID;
    }
}
