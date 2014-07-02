package com.example.create_itcoach;

import com.example.create_itcoach.beacon.BeaconMessage;
import com.example.create_itcoach.configuring.BeaconConnection;
import com.example.create_itcoach.configuring.BeaconConnectionListener;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class MyAccessibilityService extends AccessibilityService implements BeaconConnectionListener {

	BeaconConnection beaconConnection;
	boolean beaconConnected = false;
	boolean notificationIssued = false;

	@Override
	public void onServiceConnected() {
		System.out.println("SERVICE-------------------------------------------------------");
	//	beaconConnection= new BeaconConnection(this, lfb.getDevice().getAddress().trim());
	    // Set the type of events that this service wants to listen to.  Others
	    // won't be passed to this service.
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		//AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
	    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;

	    // If you only want this service to work with specific applications, set their
	    // package names here.  Otherwise, when the service is activated, it will listen
	    // to events from all applications.
	 //   info.packageNames = new String[]
	 //           {"com.example.android.myFirstApp", "com.example.android.mySecondApp"};

	    // Set the type of feedback your service will provide.
	    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

	    // Default services are invoked only if no package-specific ones are present
	    // for the type of AccessibilityEvent generated.  This service *is*
	    // application-specific, so the flag isn't necessary.  If this was a
	    // general-purpose service, it would be worth considering setting the
	    // DEFAULT flag.

	    // info.flags = AccessibilityServiceInfo.DEFAULT;

	    info.notificationTimeout = 100;

	    this.setServiceInfo(info);

	}
	
    @Override
    public void onInterrupt() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        String eventText = null;
        switch(eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventText = "Focused: ";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventText = "Focused: ";
                break;
        }

        eventText = eventText + event.getContentDescription();
        if(!beaconConnected)
        {
        	beaconConnection.Connect();
        	notificationIssued=true;
        }
        else
        {
        	
        
        }
    }

	@Override
	public void beaconConnected() {
		beaconConnected=true;
		if(notificationIssued)
		{
			int start = 150;
			int stop = 255;
			int length = 1000;
			byte bytearr[] = new byte[8];
			bytearr[0] = '0';
			bytearr[1] = '1';
			bytearr[2] = '1';
			bytearr[3] = '1';
			bytearr[4] = (byte) (start & 0xFF);
			bytearr[5] = (byte) (stop & 0xFF);
			bytearr[6] = (byte) ((length>> 8) & 0xFF);
			bytearr[7] = (byte) (length & 0xFF);
			beaconConnection.transmitHexWithoutResponse(bytearr);
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beaconSystemDisconnected() {
		beaconConnected = false;
		
	}

	@Override
	public void beaconUserDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dataReceived(BeaconMessage bm) {
		// TODO Auto-generated method stub
		
	}
}