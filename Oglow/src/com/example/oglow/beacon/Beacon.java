package com.example.oglow.beacon;

import android.bluetooth.BluetoothDevice;

public interface Beacon {
	public String getName();
	public int getMajor();
	public int getMinor();
	public double getDistance();
	public BluetoothDevice getDevice();
}
