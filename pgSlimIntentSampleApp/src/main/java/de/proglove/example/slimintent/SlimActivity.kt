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
            intent?.let {
                // match with an action, and extract relevant data from it
                when (it.action) {
                    ACTION_BARCODE_INTENT -> {
                        val barcodeContentString = intent.getStringExtra("com.symbol.datawedge.data_string")
                        val symbologyString = intent.getStringExtra("com.symbol.datawedge.label_type")
                        Log.i("Barcode", "received Barcode: $barcodeContentString with symbology: $symbologyString")
                        scannedBarcode.text = barcodeContentString ?: "null content"
                        scannedBarcodeSymbology.text = symbologyString ?: "null content"
                    }
                    ACTION_SCANNER_STATE_INTENT -> {
                        val scannerStateString = intent.getStringExtra(EXTRA_SCANNER_STATE)
                        Log.i("ScannerState", "received scanner status: $scannerStateString")
                        scannerState.text = scannerStateString ?: "null content"
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregister previously registered broadcast receiver
        if (registeredBroadcastReceiver) {
            unregisterReceiver(broadcastReceiver)
            registeredBroadcastReceiver = false
        }
    }

    companion object {

        // PG Connect Intent API constants used
        const val ACTION_SCANNER_STATE_INTENT = "com.proglove.api.SCANNER_STATE"
        const val ACTION_BARCODE_INTENT = "com.proglove.api.BARCODE"
        const val EXTRA_SCANNER_STATE = "com.proglove.api.extra.SCANNER_STATE"
    }
}
