package com.example.felix.mylights;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import java.util.ArrayList;
import android.view.View;
import android.widget.TextView;

import java.util.Set;

public class MyLightsMain extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private final static int REQUEST_ENABLE_BT = 1;
    ArrayList<String> aryLstDeviceNames = new ArrayList<String>();
    ArrayList<BluetoothDevice> aryLstBluetoothDevices = new ArrayList<BluetoothDevice>();
    Spinner spnDevices;
    Button btnConnect, btnFade;
    String bluetoothAddress;
    int spnPos,fadeIndex;
    WriteThread bluetoothOut;
    BluetoothThread bluetoothConnection;
    SeekBar selectHueBack, selectSatBack, selectValBack;
    TextView hue, sat, val;

	//Setup listeners for GUI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fadeIndex = 4;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lights_main);
        spnDevices = (Spinner) findViewById(R.id.bluetoothDevices);
        spnDevices.setOnItemSelectedListener(this);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(this);
        btnFade = (Button) findViewById(R.id.btnFade);
        btnFade.setOnClickListener(this);
        selectHueBack = (SeekBar) findViewById(R.id.hueBack);
        selectHueBack.setOnSeekBarChangeListener(this);
        selectSatBack = (SeekBar) findViewById(R.id.satBack);
        selectSatBack.setOnSeekBarChangeListener(this);
        selectValBack = (SeekBar) findViewById(R.id.valBack);
        selectValBack.setOnSeekBarChangeListener(this);
        hue = (TextView) findViewById(R.id.lblSelectHue);
        sat = (TextView) findViewById(R.id.lblSelectSat);
        val = (TextView) findViewById(R.id.lblSelectVal);
        hue.setVisibility(View.GONE);
        sat.setVisibility(View.GONE);
        val.setVisibility(View.GONE);
        selectHueBack.setVisibility(View.GONE);
        selectSatBack.setVisibility(View.GONE);
        selectValBack.setVisibility(View.GONE);
        btnFade.setVisibility(View.GONE);
        initBluetooth();
    }
	
	//Get paired bluetooth devices on phone and add name to one arraylist and device object to another. Linked with same index 
    void initBluetooth() {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, aryLstDeviceNames);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            int i = 0;
            for (BluetoothDevice device : pairedDevices) {
                String[] tempStr = new String[2];
                // Add the name and address to an array adapter to show in a ListView
                aryLstDeviceNames.add(device.getName());
                aryLstBluetoothDevices.add(device);
            }
        }
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDevices.setAdapter(mArrayAdapter);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        spnPos = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
	
	//On Click event handler for all GUI objects
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnConnect:
                bluetoothConnection = new BluetoothThread(aryLstBluetoothDevices.get(spnPos));
                int i = bluetoothConnection.connect();
                //Log.i("Assert", String.valueOf(i));
                if (i == 1) {
                    bluetoothOut = new WriteThread(bluetoothConnection.getMmSocket());
                    hue.setVisibility(View.VISIBLE);
                    sat.setVisibility(View.VISIBLE);
                    val.setVisibility(View.VISIBLE);
                    selectHueBack.setVisibility(View.VISIBLE);
                    selectSatBack.setVisibility(View.VISIBLE);
                    selectValBack.setVisibility(View.VISIBLE);
                    btnFade.setVisibility(View.VISIBLE);
                }else if(i == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Could not connect")
                            .setMessage("Please try again");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    // Create the AlertDialog
                    AlertDialog dialog = builder.create();
                }
                break;
			
			//Clicking on button cycles through fade speeds. New version has slider.
            case R.id.btnFade:
                byte bytes[] = new byte[2];
                bytes[0] = 'F';
                bytes[1] = (byte)fadeIndex;
                if(fadeIndex == 0){
                    fadeIndex = 4;
                }else{
                    fadeIndex --;
                }
                bluetoothOut.write(bytes);
                break;
        }

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }
	
	//Sliders for adjusting hue, sat, val
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        byte bytes[] = new byte[2];
        switch (seekBar.getId()) {
            case R.id.hueBack:
                bytes[0] = 'H';
                bytes[1] = (byte)progress;
                bluetoothOut.write(bytes);
                //Log.i("info",String.valueOf(progress)+ " , " + String.valueOf(value));
                break;

            case R.id.satBack:
                bytes[0] = 'S';
                bytes[1] = (byte)progress;
                bluetoothOut.write(bytes);
                //Log.i("info",String.valueOf(progress)+ " , " + String.valueOf(value));
                break;

            case R.id.valBack:
                bytes[0] = 'V';
                bytes[1] = (byte)progress;
                bluetoothOut.write(bytes);
                //Log.i("info",String.valueOf(progress)+ " , " + String.valueOf(value));
                break;
        }
    }

}