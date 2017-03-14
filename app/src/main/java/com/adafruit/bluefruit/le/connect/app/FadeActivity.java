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
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

import java.nio.ByteBuffer;

public class FadeActivity extends UartInterfaceActivity  implements ColorPicker.OnColorChangedListener {
    // Log
    private final static String TAG = FadeActivity.class.getSimpleName();

    // Constants
    private final static boolean kPersistValues = true;
    private final static String kPreferences = "ScannerActivity_prefs";
    private final static String kPreferences_color = "color";

    private final static int kFirstTimeColor = 0x0000ff;

    // UI
    private ColorPicker mColorPicker;
    private View mRgbColorView;
    private TextView mRgbTextView;

    private int mCurrentColor;
    private int mSelectedColor1;
    private int mSelectedColor2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        mBleManager = BleManager.getInstance(this);

        // UI
        mRgbColorView = findViewById(R.id.rgbColorView);
        mRgbTextView = (TextView) findViewById(R.id.rgbTextView);

        SaturationBar mSaturationBar = (SaturationBar) findViewById(R.id.saturationbar);
        ValueBar mValueBar = (ValueBar) findViewById(R.id.valuebar);
        mColorPicker = (ColorPicker) findViewById(R.id.colorPicker);
        if (mColorPicker != null) {
            mColorPicker.addSaturationBar(mSaturationBar);
            mColorPicker.addValueBar(mValueBar);
            mColorPicker.setOnColorChangedListener(this);
        }

        if (kPersistValues) {
            SharedPreferences preferences = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
            mSelectedColor1 = preferences.getInt(kPreferences_color, kFirstTimeColor);
            mSelectedColor2 = preferences.getInt(kPreferences_color, kFirstTimeColor);
        } else {
            mSelectedColor1 = kFirstTimeColor;
            mSelectedColor2 = kFirstTimeColor;
        }

        mColorPicker.setOldCenterColor(mSelectedColor1);
        mColorPicker.setOldCenterColor(mSelectedColor2);
        mColorPicker.setColor(mSelectedColor1);
        mColorPicker.setColor(mSelectedColor2);
        onColorChanged(mSelectedColor1);
        onColorChanged(mSelectedColor2);

        // Start services
        onServicesDiscovered();
    }

    @Override
    public void onStop() {
        // Preserve values
        if (kPersistValues) {
            SharedPreferences settings = getSharedPreferences(kPreferences, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(kPreferences_color, mSelectedColor1);
            editor.putInt(kPreferences_color, mSelectedColor2);
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
        intent.putExtra("title_scanner", getString(R.string.colorpicker_help_title));
        intent.putExtra("help_scanner", "colorpicker_help.html");
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

    @Override
    public void onColorChanged(int color) {
        // Save selected color
        mCurrentColor = color;

        // Update UI
        mRgbColorView.setBackgroundColor(color);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        String text = String.format(getString(R.string.colorpicker_rgbformat), r, g, b);
        mRgbTextView.setText(text);

    }

    public void onClickSetColor1(View view) {
        // Set the old color
        mColorPicker.setOldCenterColor(mSelectedColor1);
    }

    public void onClickSetColor2(View view) {
        // Set the old color
        mColorPicker.setOldCenterColor(mSelectedColor2);
    }

    public void onClickSend(View view) {

        ByteBuffer buffer = ByteBuffer.allocate(2 + 3 * 1).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // prefix
        String prefix = "!F";
        buffer.put(prefix.getBytes());

        // Send selected color !Crgb
        byte r = (byte) ((mSelectedColor1 >> 16) & 0xFF);
        byte g = (byte) ((mSelectedColor1 >> 8) & 0xFF);
        byte b = (byte) ((mSelectedColor1 >> 0) & 0xFF);

        // values
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);

        r = (byte) ((mSelectedColor2 >> 16) & 0xFF);
        g = (byte) ((mSelectedColor2 >> 8) & 0xFF);
        b = (byte) ((mSelectedColor2 >> 0) & 0xFF);

        // values
        buffer.put(r);
        buffer.put(g);
        buffer.put(b);

        byte[] result = buffer.array();
        sendDataWithCRC(result);
    }
}