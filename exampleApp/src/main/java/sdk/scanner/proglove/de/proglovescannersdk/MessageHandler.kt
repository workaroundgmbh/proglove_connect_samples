package sdk.scanner.proglove.de.proglovescannersdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import de.proglove.sdk.ConnectionStatus
import java.util.Collections
import android.content.ComponentName
import android.os.Build
import androidx.annotation.RequiresApi
import de.proglove.sdk.utils.Logger


class MessageHandler(val context: Context) : BroadcastReceiver() {

    private val scannerReceivers = Collections.synchronizedCollection(mutableSetOf<IIntentScannerOutput>())
    private val displayReceivers = Collections.synchronizedCollection(mutableSetOf<IIntentDisplayOutput>())

    val filter = IntentFilter().also {
        it.addAction(ACTION_BARCODE_INTENT)
        it.addAction(ACTION_SCANNER_STATE_INTENT)
        it.addAction(ACTION_DISPLAY_STATE_INTENT)
        it.addAction(ACTION_BUTTON_PRESSED_INTENT)
        it.addAction(ACTION_SET_SCREEN_RESULT_INTENT)
        it.addCategory(Intent.CATEGORY_DEFAULT)
    }

    private fun log(message: String) {
        Log.d(TAG, message)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            when (it.action) {
                ACTION_BARCODE_INTENT -> {
                    val barcodeContent = intent.getStringExtra(EXTRA_DATA_STRING_PG)
                    val symbology = intent.getStringExtra(EXTRA_SYMBOLOGY_STRING_PG)
                    barcodeContent?.let { s ->
                        log("received Barcode pg: $s")
                        notifyOnReceivedBarcode(s, symbology)
                    }

                }
                ACTION_SCANNER_STATE_INTENT -> {
                    log("got ACTION_SCANNER_STATE_INTENT")
                    intent.getStringExtra(EXTRA_SCANNER_STATE)?.let { s ->
                        log("got scanner $s")
                        notifyOnScannerStateChange(ConnectionStatus.valueOf(s))
                    }
                }
                ACTION_SCANNER_CONFIG -> {
                    log("got ACTION_SCANNER_CONFIG")
                    intent.getBundleExtra(EXTRA_CONFIG_BUNDLE)?.let {bundle->
                        val defaultScanFeedbackEnabled = bundle.getBoolean(EXTRA_CONFIG_DISABLE_SCAN_FEEDBACK_STRING)
                        notifyOnDefaultScanFeedbackChanged(defaultScanFeedbackEnabled)
                    }
                }

                ACTION_DISPLAY_STATE_INTENT -> {
                    log("got ACTION_D3_STATE_INTENT")
                    intent.getStringExtra(EXTRA_DISPLAY_STATE)?.let{s ->
                        log(ConnectionStatus.valueOf(s).name)
                        notifyOnDisplayStateChange(ConnectionStatus.valueOf(s))
                    }
                }
                ACTION_BUTTON_PRESSED_INTENT -> {
                    log("got ACTION_BUTTON_PRESSED_INTENT")
                    intent.getStringExtra(EXTRA_DISPLAY_BUTTON)?.let{ s ->
                        notifyOnButtonPressed(s)
                    }
                }
                ACTION_SET_SCREEN_RESULT_INTENT -> {
                    log("got ACTION_SET_SCREEN_RESULT_INTENT")
                    val success = intent.getBooleanExtra(EXTRA_DISPLAY_SET_SCREEN_SUCCESS, false)
                    var errorMessage: String? = null
                    if (!success) {
                        errorMessage = intent.getStringExtra(EXTRA_DISPLAY_SET_SCREEN_ERROR_TEXT)
                    }
                    notifyOnSetScreenSuccess(success, errorMessage)
                }
                else -> {
                    log("Unrecognized Intent")
                }
            }
        }
    }

    fun registerScannerOutput(scannerOutput: IIntentScannerOutput) {
        if (scannerReceivers.add(scannerOutput)) {
            log("$scannerOutput registered for scans")
        } else {
            log("$scannerOutput is already registered for scans. Please call unregisterScannerOutput first.")
        }
    }

    fun unregisterScannerOutput(scannerOutput: IIntentScannerOutput) {
        scannerReceivers.remove(scannerOutput)
        log("$scannerOutput removed from the list of subscribers.")
    }

    fun registerDisplayOutput(displayOutput: IIntentDisplayOutput) {
        if (displayReceivers.add(displayOutput)) {
            log("$displayOutput registered for display output")
        } else {
            log("$displayOutput is already registered for display output. Please call unregisterDisplayOutput first.")
        }
    }

    fun unregisterDisplayOutput(displayOutput: IIntentDisplayOutput) {
        displayReceivers.remove(displayOutput)
        log("$displayOutput removed from the list of subscribers.")
    }

    fun requestScannerState() {
        val intent = Intent().also {
            it.action = ACTION_GET_STATE_INTENT
        }
        sendBroadcast(intent)
    }

    fun requestDisplayState() {
        val intent = Intent().also {
            it.action = ACTION_GET_DISPLAY_STATE_INTENT
        }
        sendBroadcast(intent)
    }

    fun sendConnectD3(deviceName: String) {
        // This workaround is not meant to be sent out to any customer! For internal use only
        // We need to make sure the ProGloveService is running, because it will get killed when no device is connected.
        // So the workaround sends out a startService Intent before sending out the actual connect Intent
        // TODO: remove this workaround, when D3 pairing story is done
        // https://proglove.atlassian.net/browse/D3-540
        log("called connect")
        val serviceIntent = Intent()
        serviceIntent.component = ComponentName("de.proglove.connect", "de.proglove.core.services.ProGloveService")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            log("starting service")
            context.applicationContext.startForegroundService (serviceIntent)
        } else {
            log("Cannot start ProGlove service. Make sure it is running yourself, or connecting to Display will not work.")
        }
        log("waiting for service startup")
        Thread.sleep(2000)
        val intent = Intent().also {
            it.action = ACTION_CONNECT_DISPLAY_INTENT
            it.putExtra(EXTRA_DISPLAY_DEVICE, deviceName)
        }
        log("sending connect now")
        sendBroadcast(intent)
    }

    fun sendDisconnectD3() {
        val intent = Intent().also {
            it.action = ACTION_DISCONNECT_DISPLAY_INTENT
        }
        sendBroadcast(intent)
    }

    fun sendTestScreen(id: String, content: String, separator: String) {
        val intent = Intent().also {
            it.action = ACTION_SET_SCREEN_INTENT
            it.putExtra(EXTRA_DISPLAY_TEMPLATE_ID, id)
            it.putExtra(EXTRA_DISPLAY_DATA, content)
            it.putExtra(EXTRA_DISPLAY_SEPARATOR, separator)
            it.putExtra(EXTRA_DISPLAY_DURATION, 0)
        }
        sendBroadcast(intent)
    }

    private fun notifyOnSetScreenSuccess(success: Boolean, errorMessage: String?) {
        Toast.makeText(context, "Did receive set screen success: $success; message: $errorMessage", Toast.LENGTH_LONG).show()
    }

    private fun notifyOnButtonPressed(buttonId: String) {
        log("ButtonId: $buttonId")

        displayReceivers.forEach {
            it.onButtonPressed(buttonId)
        }
    }

    private fun notifyOnDisplayStateChange(newState: ConnectionStatus) {
        displayReceivers.forEach {
            it.onDisplayStateChanged(newState)
        }
    }

    private fun notifyOnReceivedBarcode(value: String, symbology: String?) {
        log("received Barcode $value")

        scannerReceivers.forEach {
            it.onBarcodeScanned(value, symbology)
        }
    }

    private fun notifyOnScannerStateChange(newState: ConnectionStatus) {
        log("received scannerstate $newState")
        scannerReceivers.forEach {
            it.onScannerStateChanged(newState)
        }
    }

    private fun notifyOnDefaultScanFeedbackChanged(defaultScanFeedback: Boolean) {
        log("received newDefaultScanFeedback $defaultScanFeedback")
        scannerReceivers.forEach {
            it.onScannerDefaultFeedbackUpdated(defaultScanFeedback)
        }
    }

    private fun sendBroadcast(intent: Intent) {
        context.sendBroadcast(intent)
    }

    fun triggerFeedback(feedbackSequenceId: Int) {
        val intent = Intent().also {
            it.action = ACTION_FEEDBACK_PLAY_SEQUENCE_INTENT
            it.putExtra(EXTRA_FEEDBACK_SEQUENCE_ID, feedbackSequenceId)
        }
        sendBroadcast(intent)
    }

    fun updateScannerConfig(defaultScanFeedback: Boolean) {
        val intent = Intent().also {
            it.action = ACTION_SCANNER_SET_CONFIG
            val bundle = Bundle().apply {
                putBoolean(EXTRA_CONFIG_DISABLE_SCAN_FEEDBACK_STRING,defaultScanFeedback)
            }
            it.putExtra(EXTRA_CONFIG_BUNDLE,bundle)
        }
        sendBroadcast(intent)
    }

    companion object {

        private const val TAG = "IntentApiApp:MsgHandler"

        const val ACTION_SCANNER_STATE_INTENT = "com.proglove.api.SCANNER_STATE"
        const val ACTION_BARCODE_INTENT = "com.proglove.api.BARCODE"
        const val ACTION_GET_STATE_INTENT = "com.proglove.api.GET_SCANNER_STATE"
        const val ACTION_FEEDBACK_PLAY_SEQUENCE_INTENT = "com.proglove.api.PLAY_FEEDBACK"
        const val ACTION_SCANNER_CONFIG = "com.proglove.api.CONFIG"
        const val ACTION_SCANNER_SET_CONFIG = "com.proglove.api.SET_CONFIG"

        const val ACTION_CONNECT_DISPLAY_INTENT = "com.proglove.api.DISPLAY_CONNECT"
        const val ACTION_DISCONNECT_DISPLAY_INTENT = "com.proglove.api.DISPLAY_DISCONNECT"
        const val ACTION_GET_DISPLAY_STATE_INTENT = "com.proglove.api.GET_DISPLAY_STATE"
        const val ACTION_DISPLAY_STATE_INTENT = "com.proglove.api.DISPLAY_STATE"
        const val ACTION_BUTTON_PRESSED_INTENT = "com.proglove.api.DISPLAY_BUTTON"
        const val ACTION_SET_SCREEN_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN"
        const val ACTION_SET_SCREEN_RESULT_INTENT = "com.proglove.api.SET_DISPLAY_SCREEN_RESULT"

        const val EXTRA_SCANNER_STATE = "com.proglove.api.extra.SCANNER_STATE"
        const val EXTRA_DATA_STRING_PG = "com.proglove.api.extra.BARCODE_DATA"
        const val EXTRA_SYMBOLOGY_STRING_PG = "com.proglove.api.extra.BARCODE_SYMBOLOGY"

        const val EXTRA_CONFIG_BUNDLE = "com.proglove.api.extra.CONFIG_BUNDLE"
        const val EXTRA_CONFIG_DISABLE_SCAN_FEEDBACK_STRING = "com.proglove.api.extra.config.DISABLE_SCAN_FEEDBACK"

        const val EXTRA_DISPLAY_TEMPLATE_ID = "com.proglove.api.extra.TEMPLATE_ID"
        const val EXTRA_DISPLAY_DATA = "com.proglove.api.extra.DATA"
        const val EXTRA_DISPLAY_SEPARATOR = "com.proglove.api.extra.SEPARATOR"
        const val EXTRA_DISPLAY_DURATION = "com.proglove.api.extra.DURATION"
        const val EXTRA_DISPLAY_STATE = "com.proglove.api.extra.DISPLAY_STATE"
        const val EXTRA_DISPLAY_BUTTON = "com.proglove.api.extra.DISPLAY_BUTTON"
        const val EXTRA_DISPLAY_DEVICE = "com.proglove.api.extra.DISPLAY_DEVICE_NAME"
        const val EXTRA_DISPLAY_SET_SCREEN_SUCCESS = "com.proglove.api.extra.DISPLAY_SET_SCREEN_SUCCESS"
        const val EXTRA_DISPLAY_SET_SCREEN_ERROR_TEXT =
                "com.proglove.api.extra.DISPLAY_SET_SCREEN_ERROR"
        const val EXTRA_FEEDBACK_SEQUENCE_ID = "com.proglove.api.extra.FEEDBACK_SEQUENCE_ID"
    }
}