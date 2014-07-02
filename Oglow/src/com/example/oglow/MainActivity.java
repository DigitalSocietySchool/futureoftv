package com.example.oglow;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.oglow.beacon.AbstractBeacon;
import com.example.oglow.beacon.BeaconMessage;
import com.example.oglow.beacon.GlimwormBeacon;
import com.example.oglow.configuring.BeaconConnection;
import com.example.oglow.configuring.BeaconConnectionListener;
import com.example.oglow.scanning.BLEScan;
import com.example.oglow.scanning.beaconListener;

public class MainActivity extends Activity implements beaconListener,
		BeaconConnectionListener {

	private static final float MAX_CROWD = 10;// This should be changed to the
												// maximum amount of users of
												// the system or a reference
												// number

	//private static final float maxBrightness = 0.9f;
	private static final int MAX_BRIGHTNESS = 100;
	private static final int MIN_BRIGHTNESS = 20;

	BLEScan leScanner;
	AbstractBeacon lfb = null;
	BeaconConnection beaconConnection;

	TextView statusLabel = null;

	boolean connected = false;

	boolean vibrating = false;

	boolean leach = false;

	TextView distance = null;
	TextView battery = null;

	SeekBar start, stop, length, crowd;

	private float amountCrowd;// example val

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		statusLabel = (TextView) findViewById(R.id.status_label);
		distance = (TextView) findViewById(R.id.distance);
		battery = (TextView) findViewById(R.id.battery);

		crowd = (SeekBar) findViewById(R.id.crowd); // amount of people
													// connected
		statusLabel.setText("Disconnected");
		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_ALARM);
		r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		if (savedInstanceState == null) {
		}

		// SeekBar seek = (SeekBar) findViewById(R.id.vibratespeed);

		crowd.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				amountCrowd = progress + 1;
				System.out.println("seekbar_value: " + amountCrowd);
				onStopTrackingTouch(seekBar);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// beaconConnection.transmitDataWithoutResponse("AT+PIO2" +
				// seekbar_value);
				System.out.println("seekbar_value: " + amountCrowd);
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
		// stopService(new Intent(this, MyAccessibilityService.class));
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
		if (b.getDevice().getAddress().trim().equals("20:CD:39:AE:F7:00")) {
			statusLabel.setText("Found your bracelet");
			GlimwormBeacon glb = (GlimwormBeacon) b;
			battery.setText("Battery level:" + glb.getBatteryLevel() + "%");
			lfb = b;
			if (leach) {
				distance.setText("Distance to bracelet: " + lfb.getDistance()
						+ "");
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
				beaconConnection = new BeaconConnection(this, lfb.getDevice()
						.getAddress().trim());
				beaconConnection.addListener(this, 0);
				beaconConnection.Connect();
			}
		}
	}

	public void vibrate(View v) {
		// startService(new Intent(this, MyAccessibilityService.class));
		// System.out.println("Starting Service");

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

	boolean led1on = false, led2on = false, led3on = false, led4on = false,
			led5on = false, led6on = false, led7on = false, led8on = false;

	int counter = 3;

	boolean allon = false;

	public void ledall(View v) {
		if (connected) {
			if (!allon) {
				beaconConnection.transmitData("AT+PIO31");
				beaconConnection.transmitData("AT+PIO41");
				beaconConnection.transmitData("AT+PIO51");
				beaconConnection.transmitData("AT+PIO61");
				beaconConnection.transmitData("AT+PIO71");
				beaconConnection.transmitData("AT+PIO81");
				beaconConnection.transmitData("AT+PIO91");
				beaconConnection.transmitData("AT+PIOA1");

				allon = true;
			} else {
				beaconConnection.transmitData("AT+PIO30");
				beaconConnection.transmitData("AT+PIO40");
				beaconConnection.transmitData("AT+PIO50");
				beaconConnection.transmitData("AT+PIO60");
				beaconConnection.transmitData("AT+PIO70");
				beaconConnection.transmitData("AT+PIO80");
				beaconConnection.transmitData("AT+PIO90");
				beaconConnection.transmitData("AT+PIOA0");
				allon = false;
			}
		}
	}

	public void sendwildcard(View v) {
		if (connected) {
			// beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			// beaconConnection.transmitHexData(message);
			byte bytearr[] = new byte[9];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '5';
			bytearr[4] = (byte) (1 & 0xFF);
			bytearr[5] = '0';
			bytearr[6] = '0';
			bytearr[7] = '0';
			bytearr[8] = '0';
			// beaconConnection.transmitHexData(bytearr);
			setLEDColor(255, 0, 0, 100, 1500, 1);
			setLEDColor(0, 255, 0, 100, 1500, 1);
			setLEDColor(0, 0, 255, 100, 1500, 1);
			fadeLEDColor(0, 255, 0, 1, 100, 1500, 1);
			fadeLEDColor(255, 0, 0, 1, 100, 1500, 1);
			fadeLEDColor(0, 0, 255, 1, 100, 1500, 0);
			// 30 31 31 31 FF 20 0B B8
		}
	}

	/**
	 * Crowd option: Lights will light up according to the amount of people
	 * watching the olympics. - Max amount of brightness: 90% - max users given
	 * by a variable, in this example it will be taken from a slider where max
	 * value is 10 - Lights will stay on for 15 seconds
	 */
	public void optionCrowd(View v) {
		// TextView ttv = (TextView) findViewById(R.id.wildcard);
		// amountCrowd = crowd.
		Float percentBright = calculateBrightness(amountCrowd);

		System.out.println("percentBright: " + percentBright);

		if (connected) {
			// beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			// beaconConnection.transmitHexData(message);
			byte bytearr[] = new byte[9];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '5';
			bytearr[4] = (byte) (1 & 0xFF);
			bytearr[5] = '0';
			bytearr[6] = '0';
			bytearr[7] = '0';
			bytearr[8] = '0';

			//TODO check if this part need iterations
			//for (int i = 0; i < 1; i++) {
				fadeLEDColor(255, 165, 0, 1, percentBright.intValue(), 1000, 1);
				// TODO test diff values for hold
				//set color into orange
				setLEDColor(255, 165, 0, percentBright.intValue(), 1000, 1);
				//fadeLEDColor(255, 165, 0, percentBright.intValue(), 1, 1000, 0);
			//}
			// 30 31 31 31 FF 20 0B B8
		} else {
			System.out.println("======= optionCrowd ========");
			System.out.println("Brightness " + percentBright.intValue());
		}
	}

	/**
	 * Method used to calculate the Brightness of the lights
	 * 
	 * @param amountCrowd
	 * @return
	 */
	private float calculateBrightness(float amountCrowd) {
		return ((amountCrowd * MAX_BRIGHTNESS) / MAX_CROWD) * 100;
	}

	/**
	 * Medal option: Lights will light on and off from orange to white. -
	 * Duration time is 5 seconds
	 */
	public void optionMedal(View v) {
		
		if (connected) {
			// beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			// beaconConnection.transmitHexData(message);
			byte bytearr[] = new byte[9];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '5';
			bytearr[4] = (byte) (1 & 0xFF);
			bytearr[5] = '0';
			bytearr[6] = '0';
			bytearr[7] = '0';
			bytearr[8] = '0';
			// beaconConnection.transmitHexData(bytearr);
			for (int i = 0; i < 10; i++) {
			//set color into orange
				setLEDColor(255, 165, 0, 100, 100, 0);
				setLEDColor(255, 255, 255, 100, 100, 0);
			}
			fadeLEDColor(255, 165, 0, 100, 1, 100, 0);
			
			// 30 31 31 31 FF 20 0B B8
		} else {
			System.out.println("optionMedal");
		}
	}

	/**
	 * Join in option: Lights will light an extra 10% more in relation to the amount of people
	 * watching the olympics. 
	 * - Lights will fade in and out
	 * - Max time 10 secs
	 */
	public void optionJoinIn(View v) {
		Float percentBright = calculateBrightness(amountCrowd);
		if (connected) {
			// beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			// beaconConnection.transmitHexData(message);
			byte bytearr[] = new byte[9];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '5';
			bytearr[4] = (byte) (1 & 0xFF);
			bytearr[5] = '0';
			bytearr[6] = '0';
			bytearr[7] = '0';
			bytearr[8] = '0';
			// beaconConnection.transmitHexData(bytearr);
			System.out.println("Fades in");
			fadeLEDColor(255, 165, 0, 1, MIN_BRIGHTNESS, 1000, 1);
			
			/*
			for (int i = 0; i < 3; i++) {
				fadeLEDColor(255, 165, 0, percentBright.intValue()-MIN_BRIGHTNESS, MAX_BRIGHTNESS, 1100, 1);
				//set color into orange
				setLEDColor(255, 165, 0, MAX_BRIGHTNESS, 1400, 1);
				fadeLEDColor(255, 165, 0, MAX_BRIGHTNESS, percentBright.intValue()-MIN_BRIGHTNESS, 1100, 1);
			}*/
			
			for (int i = 0; i < 3; i++) {
				fadeLEDColor(255, 165, 0, MIN_BRIGHTNESS, MAX_BRIGHTNESS, 1500, 1);
				//set color into orange
				//setLEDColor(255, 165, 0, MAX_BRIGHTNESS, 1400, 1);
				fadeLEDColor(255, 165, 0, MAX_BRIGHTNESS, MIN_BRIGHTNESS, 1500, 1);
			}
			
			fadeLEDColor(255, 165, 0, MIN_BRIGHTNESS, 0, 1000, 0);
			System.out.println("Fades out - off");
			// 30 31 31 31 FF 20 0B B8
		} else {
			System.out.println("optionJoinIn");
		}
	}
	
	/**
	 * Medal option: Lights will light on and off from orange to white. -
	 * Duration time is 5 seconds
	 */
	public void optionOff(View v) {
		Float percentBright = calculateBrightness(amountCrowd);
		if (connected) {
			// beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			// beaconConnection.transmitHexData(message);
			byte bytearr[] = new byte[9];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '5';
			bytearr[4] = (byte) (1 & 0xFF);
			bytearr[5] = '0';
			bytearr[6] = '0';
			bytearr[7] = '0';
			bytearr[8] = '0';
			
			fadeLEDColor(255, 165, 0, percentBright.intValue(), 1, 100, 0);
			
			
			// 30 31 31 31 FF 20 0B B8
		} else {
			System.out.println("optionOff");
		}
	}

	public void vibrate(int start, int stop, int duration) {
		TextView ttv = (TextView) findViewById(R.id.wildcard);
		if (connected) {
			// beaconConnection.transmitDataWithoutResponse(ttv.getText().toString());
			byte bytearr[] = new byte[8];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '1';
			bytearr[4] = (byte) (start & 0xFF);
			bytearr[5] = (byte) (stop & 0xFF);
			bytearr[6] = (byte) ((duration >> 8) & 0xFF);
			bytearr[7] = (byte) (duration & 0xFF);
			beaconConnection.transmitHexWithoutResponse(bytearr);
			System.out.println("Sending: " + new String(bytearr));
			System.out.println(((int) bytearr[4] & 0xFF) + "");
			System.out.println(((int) bytearr[5] & 0xFF) + "");
			System.out.println(((bytearr[6] & 0xFF) << 8 | (bytearr[7] & 0xFF))
					+ "");
			System.out.println("DONE");

			// 30 31 31 31 FF 20 0B B8
		}
	}

	void setLEDColor(int red, int green, int blue, int brightness,
			int duration, int hold) {
		byte bytearr[] = new byte[11];
		bytearr[0] = '0';
		bytearr[1] = '1';
		bytearr[2] = '1';
		bytearr[3] = '2';
		bytearr[4] = (byte) (red & 0xFF);
		bytearr[5] = (byte) (green & 0xFF);
		bytearr[6] = (byte) (blue & 0xFF);
		bytearr[7] = (byte) (brightness & 0xFF);
		bytearr[8] = (byte) ((duration >> 8) & 0xFF);
		bytearr[9] = (byte) (duration & 0xFF);
		bytearr[10] = (byte) (hold & 0xFF);
		beaconConnection.transmitHexData(bytearr);
	}

	void fadeLEDColor(int red, int green, int blue, int brightness,
			int brightness2, int duration, int hold) {
		byte bytearr[] = new byte[12];
		bytearr[0] = '0';
		bytearr[1] = '1';
		bytearr[2] = '1';
		bytearr[3] = '4';
		bytearr[4] = (byte) (red & 0xFF);
		bytearr[5] = (byte) (green & 0xFF);
		bytearr[6] = (byte) (blue & 0xFF);
		bytearr[7] = (byte) (brightness & 0xFF);
		bytearr[8] = (byte) (brightness2 & 0xFF);
		bytearr[9] = (byte) ((duration >> 8) & 0xFF);
		bytearr[10] = (byte) (duration & 0xFF);
		bytearr[11] = (byte) (hold & 0xFF);
		beaconConnection.transmitHexData(bytearr);
		System.out.println("Fading");
	}

	void slideLEDColor(int red, int green, int blue, int red2, int green2,
			int blue2, int brightness, int duration, int hold) {
		byte bytearr[] = new byte[14];
		bytearr[0] = '0';
		bytearr[1] = '1';
		bytearr[2] = '1';
		bytearr[3] = '3';
		bytearr[4] = (byte) (red & 0xFF);
		bytearr[5] = (byte) (green & 0xFF);
		bytearr[6] = (byte) (blue & 0xFF);
		bytearr[7] = (byte) (red2 & 0xFF);
		bytearr[8] = (byte) (green2 & 0xFF);
		bytearr[9] = (byte) (blue2 & 0xFF);
		bytearr[10] = (byte) (brightness & 0xFF);
		bytearr[11] = (byte) ((duration >> 8) & 0xFF);
		bytearr[12] = (byte) (duration & 0xFF);
		bytearr[13] = (byte) (hold & 0xFF);
		beaconConnection.transmitHexData(bytearr);
	}

}

// /////////// FOR 2

/*
 * int nl = 5000; byte bytearr[] = new byte[11]; bytearr[0] = '0'; bytearr[1] =
 * '1'; bytearr[2] = '1'; bytearr[3] = '2'; bytearr[4] = (byte)(255& 0xFF);
 * bytearr[5] = (byte) (0& 0xFF); bytearr[6] = (byte) (0& 0xFF); bytearr[7] =
 * (byte) (100& 0xFF); bytearr[8] = (byte) ((nl>> 8) & 0xFF); bytearr[9] =
 * (byte) (nl & 0xFF); bytearr[10] = (byte) (0 & 0xFF);
 * beaconConnection.transmitHexWithoutResponse(bytearr);
 * System.out.println("LOLOLOLOLOLSending: "+new String(bytearr) );
 * System.out.println(((int)bytearr[4]&0xFF)+"");
 * System.out.println(((int)bytearr[5]&0xFF)+""); System.out.println(
 * ((bytearr[6]&0xFF)<<8| (bytearr[7]&0xFF)) +""); System.out.println("DONE");
 */

// //////////// FOR 4

/*
 * int nl = 5000; byte bytearr[] = new byte[12]; bytearr[0] = '0'; bytearr[1] =
 * '1'; bytearr[2] = '1'; bytearr[3] = '4'; bytearr[4] = (byte)(255& 0xFF);
 * bytearr[5] = (byte) (0& 0xFF); bytearr[6] = (byte) (0& 0xFF); bytearr[7] =
 * (byte) (1& 0xFF); bytearr[8] = (byte) (100& 0xFF); bytearr[9] = (byte) ((nl>>
 * 8) & 0xFF); bytearr[10] = (byte) (nl & 0xFF); bytearr[11] = (byte) (0 &
 * 0xFF); beaconConnection.transmitHexWithoutResponse(bytearr);
 * System.out.println("LOLOLOLOLOLSending: "+new String(bytearr) );
 * System.out.println(((int)bytearr[4]&0xFF)+"");
 * System.out.println(((int)bytearr[5]&0xFF)+""); System.out.println(
 * ((bytearr[6]&0xFF)<<8| (bytearr[7]&0xFF)) +""); System.out.println("DONE");
 */

// ////// FOR SLIDE
/*
 * int nl = 5000; byte bytearr[] = new byte[14]; bytearr[0] = '0'; bytearr[1] =
 * '1'; bytearr[2] = '1'; bytearr[3] = '3'; bytearr[4] = (byte)(255& 0xFF);
 * bytearr[5] = (byte) (0& 0xFF); bytearr[6] = (byte) (0& 0xFF); bytearr[7] =
 * (byte) (0& 0xFF); bytearr[8] = (byte) (255& 0xFF); bytearr[9] = (byte) (0&
 * 0xFF); bytearr[10] = (byte) (100& 0xFF); bytearr[11] = (byte) ((nl>> 8) &
 * 0xFF); bytearr[12] = (byte) (nl & 0xFF); bytearr[13] = (byte) (0 & 0xFF);
 * beaconConnection.transmitHexWithoutResponse(bytearr);
 * System.out.println("LOLOLOLOLOLSending: "+new String(bytearr) );
 * System.out.println(((int)bytearr[4]&0xFF)+"");
 * System.out.println(((int)bytearr[5]&0xFF)+""); System.out.println(
 * ((bytearr[6]&0xFF)<<8| (bytearr[7]&0xFF)) +""); System.out.println("DONE");
 */
