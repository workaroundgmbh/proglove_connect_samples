package sdk.scanner.proglove.de.proglovescannersdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SlimExample : AppCompatActivity() {
    private val intentReceiver: IntentReceiver = IntentReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(intentReceiver, intentReceiver.filter)
    }
}

class IntentReceiver : BroadcastReceiver() {
    // here you need to add the right actions.
    val filter = IntentFilter().also {
        // here add the action, that datawedge and pgconnect were configured to use (set to the same string, for use of both at the same time)
        it.addAction("com.proglove.api.BARCODE")
        // this action is used to listen to Mark 2 scanner state changes, and cannot be changed
        it.addAction("com.proglove.api.SCANNER_STATE")
        it.addCategory(Intent.CATEGORY_DEFAULT)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (it.action) {
                "com.proglove.api.BARCODE" -> {
                    val barcodeContent = intent.getStringExtra("com.symbol.datawedge.data_string")
                    val symbology = intent.getStringExtra("com.symbol.datawedge.label_type")
                    Log.i("Barcode", "received Barcode: $barcodeContent with symbology: $symbology")
                }
                "com.proglove.api.SCANNER_STATE" -> {
                    val scannerState = intent.getStringExtra("com.proglove.api.extra.SCANNER_STATE")
                    Log.i("ScannerState", "received scanner status: $scannerState")
                }
                else -> {
                }
            }
        }
    }
}