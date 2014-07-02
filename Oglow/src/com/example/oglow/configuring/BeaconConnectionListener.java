package com.example.oglow.configuring;

import com.example.oglow.beacon.BeaconMessage;

public interface BeaconConnectionListener {
	public void beaconConnected();
	public void beaconSystemDisconnected();
	public void beaconUserDisconnected();
	public void dataReceived(BeaconMessage bm);
}
