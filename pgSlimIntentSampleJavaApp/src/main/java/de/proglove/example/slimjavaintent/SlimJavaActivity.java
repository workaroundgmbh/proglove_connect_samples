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
    private static final String EXTRA_SCANNER_STATE = "com.proglove.api.extra.SCANNER_STATE";

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
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case ACTION_BARCODE_INTENT: {
                            String barcodeContentString = intent.getStringExtra("com.symbol.datawedge.data_string");
                            String symbologyString = intent.getStringExtra("com.symbol.datawedge.label_type");
                            Log.i("Barcode", "received Barcode: $barcodeContentString with symbology: $symbologyString");
                            if (scannedBarcode != null) {
                                scannedBarcode.setText(barcodeContentString);
                            }
                            if (scannedBarcodeSymbology != null) {
                                scannedBarcodeSymbology.setText(symbologyString);
                            }
                        }
                        case ACTION_SCANNER_STATE_INTENT: {
                            String scannerStateString = intent.getStringExtra(EXTRA_SCANNER_STATE);
                            Log.i("ScannerState", "received scanner status: $scannerStateString");
                            if (scannerState != null) {
                                scannerState.setText(scannerStateString);
                            }
                        }
                        default: {
                            // do nothing
                        }
                    }
                }
            }
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
}
