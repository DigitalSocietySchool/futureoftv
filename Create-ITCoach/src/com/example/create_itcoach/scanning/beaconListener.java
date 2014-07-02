package com.example.create_itcoach.scanning;

import com.example.create_itcoach.beacon.AbstractBeacon;

public interface beaconListener {
	public void beaconFound(AbstractBeacon b);
	public void scanningStarted();
	public void scanningStopped();
}
