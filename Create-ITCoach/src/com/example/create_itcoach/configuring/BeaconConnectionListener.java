package com.example.create_itcoach.configuring;

import com.example.create_itcoach.beacon.BeaconMessage;

public interface BeaconConnectionListener {
	public void beaconConnected();
	public void beaconSystemDisconnected();
	public void beaconUserDisconnected();
	public void dataReceived(BeaconMessage bm);
}
