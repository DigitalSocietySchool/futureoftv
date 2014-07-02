package com.example.create_itcoach;

import com.example.create_itcoach.beacon.AbstractBeacon;
import com.example.create_itcoach.beacon.BeaconMessage;
import com.example.create_itcoach.beacon.GlimwormBeacon;
import com.example.create_itcoach.scanning.BLEScan;
import com.example.create_itcoach.scanning.beaconListener;
import com.example.create_itcoach.configuring.BeaconConnection;
import com.example.create_itcoach.configuring.BeaconConnectionListener;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.os.Build;

public class MainActivity extends Activity implements beaconListener, BeaconConnectionListener {

	BLEScan leScanner;
	AbstractBeacon lfb = null;
	BeaconConnection beaconConnection;

	TextView statusLabel = null;

	boolean connected = false;

	boolean vibrating = false;

	boolean leach = false;

	TextView distance = null;
	TextView battery = null;
	
	SeekBar start,stop,length;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		statusLabel = (TextView) findViewById(R.id.status_label);
		distance = (TextView) findViewById(R.id.distance);
		battery = (TextView) findViewById(R.id.battery);
		
		start = (SeekBar)  findViewById(R.id.startspeed);
		stop = (SeekBar)  findViewById(R.id.endspeed);
		length = (SeekBar)  findViewById(R.id.duration);
		statusLabel.setText("Disconnected");
		Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		if (savedInstanceState == null) {
		}

		SeekBar seek = (SeekBar) findViewById(R.id.vibratespeed);
		seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			int seekbar_value = 0;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seekbar_value = progress;
				onStopTrackingTouch(seekBar);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				beaconConnection.transmitDataWithoutResponse("AT+PIO2" + seekbar_value);
			}

		});
	}

	@Override
	protected void onPause() {
		if (connected) {
			leScanner.stopScan();
			beaconConnection.Disconnect();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (connected) {
			leScanner.stopScan();
			beaconConnection.Disconnect();
		}
	//	stopService(new Intent(this, MyAccessibilityService.class));
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	Ringtone r;

	@Override
	public void beaconFound(AbstractBeacon b) {
		if (b.getDevice().getAddress().trim().equals("20:CD:39:AD:68:B8")) {
			statusLabel.setText("Found your bracelet");
			GlimwormBeacon glb = (GlimwormBeacon) b;
			battery.setText("Battery level:" + glb.getBatteryLevel() + "%");
			lfb = b;
			if (leach) {
				distance.setText("Distance to bracelet: " + lfb.getDistance() + "");
				if (lfb.getDistance() > 15) {
					try {

						r.play();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					r.stop();
				}
			}
		}

	}

	public void startScan(View v) {
		if (!connected) {
			leScanner = new BLEScan(this, 2000);
			leScanner.addBeaconListener(this);
			leScanner.startScan();
		}
	}

	public void startConnect(View v) {
		if (connected) {
			beaconConnection.Disconnect();
		} else {
			if (lfb != null) {
				leScanner.stopScan();
				beaconConnection = new BeaconConnection(this, lfb.getDevice().getAddress().trim());
				beaconConnection.addListener(this, 0);
				beaconConnection.Connect();
			}
		}
	}

	public void vibrate(View v) {
	//	startService(new Intent(this, MyAccessibilityService.class));
		System.out.println("Starting Service");
		/*
		if (connected) {
			if (!vibrating) {
				beaconConnection.transmitDataWithoutResponse("AT+PIO21");
				statusLabel.setText("Shaking your bracelet");
				vibrating = true;
			} else {
				beaconConnection.transmitDataWithoutResponse("AT+PIO20");
				statusLabel.setText("Connected to your bracelet");
				vibrating = false;
			}
		}
		*/
	}

	public void leach(View v) {
		if (leScanner != null)
			leScanner.stopScan();
		leScanner = new BLEScan(this, 2000);
		leScanner.addBeaconListener(this);
		leScanner.startIntervalScan(5000);
		leach = true;
	}

	@Override
	public void beaconConnected() {
		statusLabel.setText("Connected to your bracelet");
		connected = true;
		TextView tv = (TextView) findViewById(R.id.connect);
		tv.setText("Disconnect");
	}

	@Override
	public void beaconSystemDisconnected() {
		statusLabel.setText("Disconnected");
		connected = false;
		TextView tv = (TextView) findViewById(R.id.connect);
		tv.setText("Connect");
	}

	@Override
	public void beaconUserDisconnected() {
		statusLabel.setText("Disconnected");
		connected = false;
		TextView tv = (TextView) findViewById(R.id.connect);
		tv.setText("Connect");
	}

	@Override
	public void dataReceived(BeaconMessage bm) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scanningStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void scanningStopped() {
		// TODO Auto-generated method stub

	}

	boolean led1on = false, led2on = false, led3on = false, led4on = false, led5on = false, led6on = false, led7on = false, led8on = false;

	public void ledOne(View v) {
		if (connected) {
			if (!led1on) {
				beaconConnection.transmitData("AT+PIOB1");
				led1on = true;
			} else {
				beaconConnection.transmitData("AT+PIOB0");
				led1on = false;
			}
		}
	}

	public void ledTwo(View v) {
		if (connected) {
			if (!led2on) {
				beaconConnection.transmitData("AT+PIO71");
				led2on = true;
			} else {
				beaconConnection.transmitData("AT+PIO70");
				led2on = false;
			}
		}
	}

	public void ledThree(View v) {
		if (connected) {
			if (!led3on) {
				beaconConnection.transmitData("AT+PIO21");
				led3on = true;
			} else {
				beaconConnection.transmitData("AT+PIO20");
				led3on = false;
			}
		}
	}

	public void sendwildcard(View v) {
		TextView ttv = (TextView) findViewById(R.id.wildcard);
		if (connected) {
		//	beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			byte bytearr[] = new byte[8];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '1';
			bytearr[4] = (byte) (start.getProgress() & 0xFF);
			bytearr[5] = (byte) (stop.getProgress() & 0xFF);
			bytearr[6] = (byte) ((length.getProgress()>> 8) & 0xFF);
			bytearr[7] = (byte) (length.getProgress() & 0xFF);
			beaconConnection.transmitHexWithoutResponse(bytearr);
			System.out.println("Sending: "+new String(bytearr) );
				System.out.println(((int)bytearr[4]&0xFF)+"");
				System.out.println(((int)bytearr[5]&0xFF)+"");
				System.out.println(      ((bytearr[6]&0xFF)<<8| (bytearr[7]&0xFF))            +"");
				System.out.println("DONE");
		
			// 30 31 31 31 FF 20 0B B8
		}
	}


	public void vibrate(int start,int stop,int duration) {
		TextView ttv = (TextView) findViewById(R.id.wildcard);
		if (connected) {
		//	beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			byte bytearr[] = new byte[8];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '1';
			bytearr[4] = (byte) (start & 0xFF);
			bytearr[5] = (byte) (stop & 0xFF);
			bytearr[6] = (byte) ((duration>> 8) & 0xFF);
			bytearr[7] = (byte) (duration & 0xFF);
			beaconConnection.transmitHexWithoutResponse(bytearr);
			System.out.println("Sending: "+new String(bytearr) );
				System.out.println(((int)bytearr[4]&0xFF)+"");
				System.out.println(((int)bytearr[5]&0xFF)+"");
				System.out.println(      ((bytearr[6]&0xFF)<<8| (bytearr[7]&0xFF))            +"");
				System.out.println("DONE");
		
			// 30 31 31 31 FF 20 0B B8
		}
	}

	
	
}
