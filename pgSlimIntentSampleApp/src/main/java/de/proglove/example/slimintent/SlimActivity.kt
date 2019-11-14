package de.proglove.example.slimintent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * This is a minimal code with minimal dependencies that listens to a subset of PG Connect Intent API (barcode scans,
 * and scanner state changes only).
 */
class SlimActivity : AppCompatActivity() {

    // a variable to keep track of the broadcast receiver's registration state
    private var registeredBroadcastReceiver = false

    // intent filer for PG Intent API, here you need to add the right actions.
    private val intentFilter = IntentFilter().also {
        // here add the action, that DataWedge and PG Connect were configured to use (set to the same string, to use
        // both of them at the same time).
        it.addAction(ACTION_BARCODE_INTENT)
        // this action is used to listen to Mark 2 scanner state changes, and cannot be changed
        it.addAction(ACTION_SCANNER_STATE_INTENT)
        it.addCategory(Intent.CATEGORY_DEFAULT)
    }

    // broadcast receiver object for intents
    private val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            // Handle broadcasted Intents with action
            handleNewIntent(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // register broadcast receiver, if not done yet
        if (!registeredBroadcastReceiver) {
            registerReceiver(broadcastReceiver, intentFilter)
            registeredBroadcastReceiver = true
        }

        // Handle intent sent with start activity action which created this activity.
        // That Intent will not trigger #onNewIntent.
        handleNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle intent sent with start activity action
        handleNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregister previously registered broadcast receiver
        if (registeredBroadcastReceiver) {
            unregisterReceiver(broadcastReceiver)
            registeredBroadcastReceiver = false
        }
    }

    /**
     * Parses new Intent and updates scanned data on UI, if there is any.
     */
    private fun handleNewIntent(intent: Intent?) {
        intent?.let {
            // match with an action, and extract relevant data from it
            when (it.action) {
                ACTION_BARCODE_VIA_START_ACTIVITY_INTENT,
                ACTION_BARCODE_INTENT -> {
                    handleScannedBarcode(intent)
                }
                ACTION_SCANNER_STATE_INTENT -> {
                    val scannerStateString = intent.getStringExtra(EXTRA_SCANNER_STATE)
                    Log.i(TAG, "received scanner status: $scannerStateString")
                    scannerState.text = scannerStateString ?: "null content"
                }
                else -> {
                    if (intent.hasExtra(EXTRA_DATA_STRING) || intent.hasExtra(EXTRA_SYMBOLOGY_STRING)) {
                        handleScannedBarcode(intent)
                    }
                }
            }
        }
    }

    /**
     * Gets scanned barcode data received in intent and updates UI.
     */
    private fun handleScannedBarcode(intent: Intent) {
        val barcodeContentString = intent.getStringExtra(EXTRA_DATA_STRING)
        val symbologyString = intent.getStringExtra(EXTRA_SYMBOLOGY_STRING)
        Log.i(TAG, "received Barcode: $barcodeContentString with symbology: $symbologyString")
        scannedBarcode.text = barcodeContentString ?: "null content"
        scannedBarcodeSymbology.text = symbologyString ?: "null content"
    }

    companion object {
        const val TAG = "SlimActivity"

        // PG Connect Intent API constants used
        const val ACTION_SCANNER_STATE_INTENT = "com.proglove.api.SCANNER_STATE"
        const val ACTION_BARCODE_INTENT = "com.proglove.api.BARCODE"
        const val ACTION_BARCODE_VIA_START_ACTIVITY_INTENT = "com.proglove.api.BARCODE_START_ACTIVITY"
        const val EXTRA_SCANNER_STATE = "com.proglove.api.extra.SCANNER_STATE"
        const val EXTRA_DATA_STRING = "com.symbol.datawedge.data_string"
        const val EXTRA_SYMBOLOGY_STRING = "com.symbol.datawedge.label_type"
    }
}
