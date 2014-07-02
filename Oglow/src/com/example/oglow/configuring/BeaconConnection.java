package com.example.oglow.configuring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.oglow.beacon.BeaconMessage;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class BeaconConnection {

	private Activity activity;
	private String deviceAddress;

	private BluetoothLeService mBluetoothLeService;

	private List<BeaconConnectionListener> bcls = new ArrayList<BeaconConnectionListener>();

	private boolean isConnected = false;
	private BluetoothGattCharacteristic characteristicTX;
	private BluetoothGattCharacteristic characteristicRX;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";

	private List<String> transmitQueue = new ArrayList<String>();
	private List<Byte[]> transmitByteQueue = new ArrayList<Byte[]>();
	
	private boolean sendingHex = false;

	private boolean transmitting = false;
	private boolean disconnectAfterQueueEmpty = false;

	public BeaconConnection(Activity activity, String deviceAddress) {
		this.activity = activity;
		this.deviceAddress = deviceAddress;
	}

	public void Connect() {
		Intent gattServiceIntent = new Intent(activity, BluetoothLeService.class);
		activity.bindService(gattServiceIntent, mServiceConnection, Activity.BIND_AUTO_CREATE);
		activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	public void Disconnect() {
		if (!disconnectAfterQueueEmpty && transmitting) {
			disconnectAfterQueueEmpty = true;
			return;
		}
		if (disconnectAfterQueueEmpty && transmitQueue.size() > 0)
			return;
		isConnected = false;
		if (mBluetoothLeService != null) {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();
			activity.unregisterReceiver(mGattUpdateReceiver);
			activity.unbindService(mServiceConnection);
			mBluetoothLeService = null;
			disconnectAfterQueueEmpty = false;
			for (BeaconConnectionListener bcl : bcls)
				bcl.beaconUserDisconnected();
		}
	}

	public void Reconnect() {
		if (mBluetoothLeService != null) {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();
		}
		activity.unregisterReceiver(mGattUpdateReceiver);
		activity.unbindService(mServiceConnection);
		mBluetoothLeService = null;
		Connect();

	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
			//	Log.e(ConfigureBeacon.class.getName(), "Unable to initialize Bluetooth");
				activity.finish();
			}
			mBluetoothLeService.connect(deviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				// DO NOT ALERT LISTENERS YET, BLE CONNECTION
				// WITH PIN CODE ON ANDROID IS UNSTABLE. USE THE GATT SERVICES
				// DISCOVERED AS AN IDENTIFIER OF A PROPPER BEACON CONNECTION!!
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				isConnected = false;
				for (BeaconConnectionListener bcl : bcls)
					bcl.beaconSystemDisconnected();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				List<BluetoothGattService> gattServices = mBluetoothLeService.getSupportedGattServices();
				if (gattServices == null)
					return;
				String unknownServiceString = "";
				for (BluetoothGattService gattService : gattServices) {
					String uuid = gattService.getUuid().toString();
					if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {
						characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
						characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
						isConnected = true;
						for (BeaconConnectionListener bcl : bcls)
							bcl.beaconConnected();
						break;
					}
				}
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				if (transmitQueue.size() == 0 && !sendingHex)
					return;
				if (transmitByteQueue.size() == 0 && sendingHex)
					return;
				for (BeaconConnectionListener bcl : bcls){
					if(!sendingHex)bcl.dataReceived(new BeaconMessage(transmitQueue.get(0), intent.getStringExtra(mBluetoothLeService.EXTRA_DATA)));
					else bcl.dataReceived(new BeaconMessage(transmitByteQueue.get(0).toString(), intent.getStringExtra(mBluetoothLeService.EXTRA_DATA)));
				}
				transmitting = false;
				if(!sendingHex)transmitQueue.remove(0);
				else transmitByteQueue.remove(0);
				
				if (disconnectAfterQueueEmpty && ((transmitQueue.size() == 0 && !sendingHex)  || (transmitByteQueue.size()==0 && sendingHex))) {
					Disconnect();
				} else
					if(!sendingHex) transmitData();
					else transmitHexData();

			}
		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	public void addListener(BeaconConnectionListener bcl, int priority) {
		if (priority > 0)
			bcls.add(0, bcl);
		else
			bcls.add(bcl);
	}

	private void transmitData() {
		if (transmitQueue.size() > 0 && !transmitting) {
			if (mBluetoothLeService != null && characteristicTX != null) {
				sendingHex=false;
				characteristicTX.setValue(transmitQueue.get(0));
				mBluetoothLeService.writeCharacteristic(characteristicTX);
				mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
				transmitting = true;
			}
		}
	}

	public void transmitData(String data) {
		transmitQueue.add(data);
		transmitData();
	}

	public void transmitData(List<String> data) {
		transmitQueue.addAll(data);
		transmitData();
	}

	public void resetTransmission() {
		transmitting = false;
		transmitQueue.clear();
		transmitByteQueue.clear();
	}

	public void transmitDataWithoutResponse(String data) {

		if (transmitting || transmitQueue.size() > 0) {

		}

		transmitQueue.add(data);
		transmitData();
		transmitting = false;
		transmitQueue.remove(0);
	}
	
	public void transmitHexWithoutResponse(byte data[]) {

		if ( !transmitting) {
			if (mBluetoothLeService != null && characteristicTX != null) {
				
				characteristicTX.setValue(data);
				mBluetoothLeService.writeCharacteristic(characteristicTX);
				
				mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
				transmitting = true;
			}
		}
		transmitting = false;
	}
	
	public void transmitHexData(byte[] message)
	{
		Byte tmp[] = new Byte[message.length];
		for(int i=0;i<message.length;i++)
		{
			tmp[i] = message[i];
		}
		transmitByteQueue.add(tmp);
		transmitHexData();
	}
	
	private void transmitHexData() {
		if (transmitByteQueue.size() > 0 && !transmitting) {
			if (mBluetoothLeService != null && characteristicTX != null) {
				byte tmp[] = new byte[transmitByteQueue.get(0).length];
				for(int i=0;i<transmitByteQueue.get(0).length;i++)
				{
					tmp[i] = transmitByteQueue.get(0)[i];
					System.out.println(transmitByteQueue.get(0)[i]);
				}
				sendingHex=true;
				characteristicTX.setValue(tmp);
				mBluetoothLeService.writeCharacteristic(characteristicTX);
				mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
				transmitting = true;
			}
		}
	}
}
