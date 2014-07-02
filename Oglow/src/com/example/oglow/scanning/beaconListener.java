package com.example.oglow.scanning;

import com.example.oglow.beacon.AbstractBeacon;

public interface beaconListener {
	public void beaconFound(AbstractBeacon b);
	public void scanningStarted();
	public void scanningStopped();
}
