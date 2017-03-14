package com.adafruit.bluefruit.le.connect.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.adafruit.bluefruit.le.connect.R;
import com.adafruit.bluefruit.le.connect.ble.BleManager;

import java.nio.ByteBuffer;

public class PressureActivity extends UartInterfaceActivity {
    // Log
    private final static String TAG = PressureActivity.class.getSimpleName();

    // Constants
    private final static boolean kPersistValues = true;
    private final static String kPreferences = "PressureActivity_prefs";
    private final static String kPreferences_color = "color";

    private final static int kFirstTimeColor = 0x0000ff;

    // UI
    private View mRgbColorView;
    private TextView mRgbTextView;

    private int mSelectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure);

        mBleManager = BleManager.getInstance(this);

        // Start services
        onServicesDiscovered();
    }

    @Override
    public void onStop() {
        // Preserve values
        if (kPersistValues) {
            SharedPreferences settings = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(kPreferences_color, mSelectedColor);
            editor.apply();
        }

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            startHelp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startHelp() {
        // Launch help activity
        Intent intent = new Intent(this, CommonHelpActivity.class);
        intent.putExtra("title_pressure", getString(R.string.colorpicker_help_title));
        intent.putExtra("help_pressure", "colorpicker_help.html");
        startActivity(intent);
    }


    // region BleManagerListener
    /*
    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }
*/
    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Log.d(TAG, "Disconnected. Back to previous activity");
        setResult(-1);      // Unexpected Disconnect
        finish();
    }
    /*

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }
*/
    // endregion


    public void onClickSendStart(View view) {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        Log.d(TAG, "onClickSendStart");

        // prefix
        String prefix = "!P";
        buffer.put(prefix.getBytes());

        byte[] result = buffer.array();
        sendDataWithCRC(result);

    }

    public void onClickSendStop(View view) {
        ByteBuffer buffer = ByteBuffer.allocate(2).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        Log.d(TAG, "onClickSendStop");

        // prefix
        String prefix = "!Q";
        buffer.put(prefix.getBytes());

        byte[] result = buffer.array();
        sendDataWithCRC(result);
    }
}