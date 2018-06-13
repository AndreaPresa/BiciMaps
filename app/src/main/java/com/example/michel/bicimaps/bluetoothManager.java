package com.example.michel.bicimaps;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import java.util.UUID;

public class bluetoothManager {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private static final UUID MY_UUID_INSECURE= UUID.fromString(
            "00000000-0000-1000-8000-00805F9B34FB");
    private Handler mBluetoothHandler;

    public bluetoothManager(){

    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BluetoothDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

}

