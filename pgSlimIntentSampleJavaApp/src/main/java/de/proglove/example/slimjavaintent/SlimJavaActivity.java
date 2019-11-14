package de.proglove.example.slimjavaintent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SlimJavaActivity extends AppCompatActivity {

    // PG Connect Intent API constants used
    private static final String ACTION_SCANNER_STATE_INTENT = "com.proglove.api.SCANNER_STATE";
    private static final String ACTION_BARCODE_INTENT = "com.proglove.api.BARCODE";
    private static final String ACTION_BARCODE_VIA_START_ACTIVITY_INTENT = "com.proglove.api.BARCODE_START_ACTIVITY";
    private static final String EXTRA_SCANNER_STATE = "com.proglove.api.extra.SCANNER_STATE";
    private static final String EXTRA_DATA_STRING = "com.symbol.datawedge.data_string";
    private static final String EXTRA_SYMBOLOGY_STRING = "com.symbol.datawedge.label_type";

    private static String LOG_TAG = SlimJavaActivity.class.getSimpleName();

    // a variable to keep track of the broadcast receiver's registration state
    private boolean registeredBroadcastReceiver = false;

    // views
    private TextView scannedBarcode = null;
    private TextView scannedBarcodeSymbology = null;
    private TextView scannerState = null;

    // broadcast receiver object for intents
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle broadcasted Intents with action
            handleNewIntent(intent);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // prepare views
        scannedBarcode = findViewById(R.id.scannedBarcode);
        scannedBarcodeSymbology = findViewById(R.id.scannedBarcodeSymbology);
        scannerState = findViewById(R.id.scannerState);

        // here you need to add the right actions.
        IntentFilter intentFilter = new IntentFilter();

        // here add the action, that DataWedge and PG Connect were configured to use (set to the same string, to use
        // both of them at the same time).
        intentFilter.addAction(ACTION_BARCODE_INTENT);
        // this action is used to listen to Mark 2 scanner state changes, and cannot be changed
        intentFilter.addAction(ACTION_SCANNER_STATE_INTENT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // register broadcast receiver, if not done yet
        if (!registeredBroadcastReceiver) {
            registerReceiver(broadcastReceiver, intentFilter);
            registeredBroadcastReceiver = true;
        }

        // Handle intent sent with start activity action which created this activity.
        // That Intent will not trigger #onNewIntent.
        handleNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle intent sent with start activity action
        handleNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister previously registered broadcast receiver
        if (registeredBroadcastReceiver) {
            unregisterReceiver(broadcastReceiver);
            registeredBroadcastReceiver = false;
        }
    }

    /**
     * Parses new Intent and updates scanned data on UI, if there is any.
     */
    private void handleNewIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_BARCODE_INTENT:
                    case ACTION_BARCODE_VIA_START_ACTIVITY_INTENT: {
                        handleScannedBarcode(intent);
                    }
                    case ACTION_SCANNER_STATE_INTENT: {
                        String scannerStateString = intent.getStringExtra(EXTRA_SCANNER_STATE);
                        Log.i(LOG_TAG, "received scanner status: $scannerStateString");
                        if (scannerState != null) {
                            scannerState.setText(scannerStateString);
                        }
                    }
                    default: {
                        // do nothing
                    }
                }
            } else if (intent.hasExtra(EXTRA_DATA_STRING)
                    || intent.hasExtra(EXTRA_SYMBOLOGY_STRING)) {
                handleScannedBarcode(intent);
            }
        }
    }

    /**
     * Gets scanned barcode data received in intent and updates UI.
     */
    private void handleScannedBarcode(Intent intent) {
        String barcodeContentString = intent.getStringExtra(EXTRA_DATA_STRING);
        String symbologyString = intent.getStringExtra(EXTRA_SYMBOLOGY_STRING);
        Log.i(LOG_TAG, "received Barcode: " + barcodeContentString + " with symbology: " + symbologyString);
        if (scannedBarcode != null) {
            scannedBarcode.setText(barcodeContentString);
        }
        if (scannedBarcodeSymbology != null) {
            scannedBarcodeSymbology.setText(symbologyString);
        }
    }
}
