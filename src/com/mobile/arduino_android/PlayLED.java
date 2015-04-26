/**
 * DO IT YOURSELF: BURGLAR ALARM
 * Arno Puder, Pratik Jaiswal
 */

package com.mobile.arduino_android;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * This application is responsible for handling the LED inserted into the PIN 13
 * of arduino board. In order to have a successful communication with the Arduino 
 * Board via Bluetooth, we need two addresses: Serial Service Port (SSP) UUID and 
 * Mac Address for pairing Bluetooth module with this LED application. SSP UUID is 
 * generally 0x1101 which is a substring of 00001101-0000-1000-8000-00805F9B34FB.  
 * @author jpratik
 *
 */
 
public class PlayLED extends Activity {
  private static final String TAG = "PlayLED";
   
  Button btnOn, btnOff;
   
  private static final int REQUEST_ENABLE_BT = 1;
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private OutputStream outStream = null;
   
  /*
   * Universal Unique Identifier address is 1101 which is a substring of default address:
   * 00001101-0000-1000-8000-00805F9B34FB and the way to find the MAC address is to see the 
   * Bluetooth connections in a computer and right click on the BT device to see its MAC address 
   */
  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private static String address = "30:14:11:12:14:37";
   
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
 
    Log.d(TAG, "In onCreate()");
 
    setContentView(R.layout.activity_play_led);
 
    btnOn = (Button) findViewById(R.id.Button0N);
    btnOff = (Button) findViewById(R.id.ButtonOFF);
    
    //Get the default BT adapter with BluetoothAdapter class
    btAdapter = BluetoothAdapter.getDefaultAdapter();
    checkBTState();
 
    btnOn.setOnClickListener(new OnClickListener() {
      @Override
	public void onClick(View v) {
        sendData("1");
        Toast msg = Toast.makeText(getBaseContext(),
            "You have clicked On", Toast.LENGTH_SHORT);
        msg.show();
      }
    });
 
    btnOff.setOnClickListener(new OnClickListener() {
      @Override
	public void onClick(View v) {
        sendData("0");
        Toast msg = Toast.makeText(getBaseContext(),
            "You have clicked Off", Toast.LENGTH_SHORT);
        msg.show();
      }
    });
  }
   
  @Override
  public void onResume() {
    super.onResume();
 
    Log.d(TAG, "...In onResume - Attempting client connect...");
   
    /*
     *  Set up a pointer to the BT module by using its MAC address. We can 
     *  use getRemoteDevice() method provided by BluetoothAdapter class.
     */
    BluetoothDevice device = btAdapter.getRemoteDevice(address);
   
    
    //Create a socket for RF communication using UUID. 
    try {
    		btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    }catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }
   
    /*
     * Bluetooth discovery is resource intensive and so, we need 
     * to cancel the discovery now (as we are already creating socket) 
     */
    
    btAdapter.cancelDiscovery();
   
    Log.d(TAG, "...Connecting to Remote...");
    try {
      btSocket.connect();
      Log.d(TAG, "...Connection established and data link opened...");
    } catch (IOException e) {
      try {
        btSocket.close();
      } catch (IOException e2) {
        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    }
     
    Log.d(TAG, "...Creating Socket...");
 
    try {
      outStream = btSocket.getOutputStream();
    } catch (IOException e) {
      errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
    }
  }
 
  @Override
  public void onPause() {
    super.onPause();
 
    Log.d(TAG, "...In onPause()...");
 
    if (outStream != null) {
      try {
        outStream.flush();
      } catch (IOException e) {
        errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
      }
    }
 
    try     {
      btSocket.close();
    } catch (IOException e2) {
      errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
    }
  }
   
  private void checkBTState() {
       if(btAdapter==null) { 
      errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
    } else {
      if (btAdapter.isEnabled()) {
        Log.d(TAG, "...Bluetooth is enabled...");
      } else {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }
    }
  }
 
  private void errorExit(String title, String message){
    Toast msg = Toast.makeText(getBaseContext(),
        title + " - " + message, Toast.LENGTH_SHORT);
    msg.show();
    finish();
  }
 
  private void sendData(String message) {
    byte[] msgBuffer = message.getBytes();
 
    Log.d(TAG, "...Sending data: " + message + "...");
 
    try {
      outStream.write(msgBuffer);
    } catch (IOException e) {
      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
      
      /*
       * By default the MAC address will be set to 00:00:00:00:00:00 and so you need to change it
       * according to your BT module's address (how to see it is given above in description of app)
       */
      
      if (address.equals("00:00:00:00:00:00")) 
    	  msg = msg + ".\n\nPlease update your MAC address from 00:00:00:00:00:00 to the correct address";
      errorExit("Fatal Error", msg);       
    }
  }
}