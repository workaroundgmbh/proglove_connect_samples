package de.proglove.example.intent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import de.proglove.example.common.ApiConstants
import de.proglove.example.intent.enums.DeviceConnectionStatus
import de.proglove.example.intent.interfaces.IIntentDisplayOutput
import de.proglove.example.intent.interfaces.IIntentScannerOutput
import java.util.*

/**
 * Message handler is a helper class to work with broadcasts.
 */
class MessageHandler(private val context: Context) : BroadcastReceiver() {

    // callbacks for scanner and display functions
    private val scannerReceivers = Collections.synchronizedCollection(mutableSetOf<IIntentScannerOutput>())
    private val displayReceivers = Collections.synchronizedCollection(mutableSetOf<IIntentDisplayOutput>())

    // intent filer to filer out PG Intent API actions
    val filter = IntentFilter().also {
        it.addAction(ApiConstants.ACTION_BARCODE_INTENT)
        it.addAction(ApiConstants.ACTION_SCANNER_STATE_INTENT)
        it.addAction(ApiConstants.ACTION_DISPLAY_STATE_INTENT)
        it.addAction(ApiConstants.ACTION_BUTTON_PRESSED_INTENT)
        it.addAction(ApiConstants.ACTION_SET_SCREEN_RESULT_INTENT)
        it.addCategory(Intent.CATEGORY_DEFAULT)
    }

    /**
     * A method overridden from the [BroadcastReceiver] to intercept caught intents.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            // match actions specified in the intent filer and extract relevant data accordingly
            when (it.action) {
                ApiConstants.ACTION_BARCODE_INTENT -> {
                    val barcodeContent = intent.getStringExtra(ApiConstants.EXTRA_DATA_STRING_PG)
                    val symbology = intent.getStringExtra(ApiConstants.EXTRA_SYMBOLOGY_STRING_PG)
                    barcodeContent?.let { s ->
                        log("received Barcode pg: $s")
                        notifyOnReceivedBarcode(s, symbology)
                    }
                }
                ApiConstants.ACTION_SCANNER_STATE_INTENT -> {
                    log("got ACTION_SCANNER_STATE_INTENT")
                    intent.getStringExtra(ApiConstants.EXTRA_SCANNER_STATE)?.let { s ->
                        log("got scanner $s")
                        notifyOnScannerStateChange(DeviceConnectionStatus.valueOf(s))
                    }
                }
                ApiConstants.ACTION_SCANNER_CONFIG -> {
                    log("got ACTION_SCANNER_CONFIG")
                    intent.getBundleExtra(ApiConstants.EXTRA_CONFIG_BUNDLE)?.let { bundle ->
                        val defaultScanFeedbackEnabled =
                            bundle.getBoolean(ApiConstants.EXTRA_CONFIG_DISABLE_SCAN_FEEDBACK_STRING)
                        notifyOnDefaultScanFeedbackChanged(defaultScanFeedbackEnabled)
                    }
                }

                ApiConstants.ACTION_DISPLAY_STATE_INTENT -> {
                    log("got ACTION_D3_STATE_INTENT")
                    intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_STATE)?.let { s ->
                        log(DeviceConnectionStatus.valueOf(s).name)
                        notifyOnDisplayStateChange(DeviceConnectionStatus.valueOf(s))
                    }
                }
                ApiConstants.ACTION_BUTTON_PRESSED_INTENT -> {
                    log("got ACTION_BUTTON_PRESSED_INTENT")
                    intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_BUTTON)?.let { s ->
                        notifyOnButtonPressed(s)
                    }
                }
                ApiConstants.ACTION_SET_SCREEN_RESULT_INTENT -> {
                    log("got ACTION_SET_SCREEN_RESULT_INTENT")
                    val success = intent.getBooleanExtra(ApiConstants.EXTRA_DISPLAY_SET_SCREEN_SUCCESS, false)
                    var errorMessage: String? = null
                    if (!success) {
                        errorMessage = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SET_SCREEN_ERROR_TEXT)
                    }
                    notifyOnSetScreenSuccess(success, errorMessage)
                }
                else -> {
                    log("Unrecognized Intent")
                }
            }
        }
    }

    /**
     * Register a callback for scanner actions.
     *
     * @param scannerOutput your scanner output callback object.
     */
    fun registerScannerOutput(scannerOutput: IIntentScannerOutput) {
        if (scannerReceivers.add(scannerOutput)) {
            log("$scannerOutput registered for scans")
        } else {
            log("$scannerOutput is already registered for scans. Please call unregisterScannerOutput first.")
        }
    }

    /**
     * Unregister a callback for scanner actions, that's been registered earlier.
     *
     * @param scannerOutput your scanner output callback object.
     */
    fun unregisterScannerOutput(scannerOutput: IIntentScannerOutput) {
        scannerReceivers.remove(scannerOutput)
        log("$scannerOutput removed from the list of subscribers.")
    }

    /**
     * Register a callback for display actions.
     *
     * @param displayOutput your display output callback object.
     */
    fun registerDisplayOutput(displayOutput: IIntentDisplayOutput) {
        if (displayReceivers.add(displayOutput)) {
            log("$displayOutput registered for display output")
        } else {
            log("$displayOutput is already registered for display output. Please call unregisterDisplayOutput first.")
        }
    }

    /**
     * Unregister a callback for display actions, that's been registered earlier.
     *
     * @param displayOutput your display output callback object.
     */
    fun unregisterDisplayOutput(displayOutput: IIntentDisplayOutput) {
        displayReceivers.remove(displayOutput)
        log("$displayOutput removed from the list of subscribers.")
    }

    /**
     * Request current scanner state to be broadcasted and sent down to the registered scanner callback output.
     */
    fun requestScannerState() {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_GET_STATE_INTENT
        }
        sendBroadcast(intent)
    }

    /**
     * Request current display state to be broadcasted and sent down to the registered display callback output.
     */
    fun requestDisplayState() {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_GET_DISPLAY_STATE_INTENT
        }
        sendBroadcast(intent)
    }

    /**
     * Disconnect display (D3).
     */
    fun sendDisconnectD3() {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_DISCONNECT_DISPLAY_INTENT
        }
        sendBroadcast(intent)
    }

    /**
     * Send a test screen to the display.
     *
     * @param id template ID.
     * @param content data for display.
     * @param separator separator.
     */
    fun sendTestScreen(id: String, content: String, separator: String) {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_SET_SCREEN_INTENT
            it.putExtra(ApiConstants.EXTRA_DISPLAY_TEMPLATE_ID, id)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_DATA, content)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_SEPARATOR, separator)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_DURATION, 0)
        }
        sendBroadcast(intent)
    }

    /**
     * Show a toast with set screen result.
     *
     * @param success did it succeed
     * @param errorMessage error message, if did not succeed.
     */
    private fun notifyOnSetScreenSuccess(success: Boolean, errorMessage: String?) {
        Toast.makeText(context, "Did receive set screen success: $success; message: $errorMessage", Toast.LENGTH_LONG)
            .show()
    }

    /**
     * Notify display receivers of a button press on D3.
     */
    private fun notifyOnButtonPressed(buttonId: String) {
        log("ButtonId: $buttonId")

        displayReceivers.forEach {
            it.onButtonPressed(buttonId)
        }
    }

    /**
     * Notify display receivers of a D3 state change.
     *
     * @param newState the new connection state.
     */
    private fun notifyOnDisplayStateChange(newState: DeviceConnectionStatus) {
        displayReceivers.forEach {
            it.onDisplayStateChanged(newState)
        }
    }

    /**
     * Notify scanner receivers when a barcode scan has been received.
     *
     * @param value scanned barcode string.
     * @param symbology symbology of that barcode, if supported.
     */
    private fun notifyOnReceivedBarcode(value: String, symbology: String?) {
        log("received Barcode $value")

        scannerReceivers.forEach {
            it.onBarcodeScanned(value, symbology)
        }
    }

    /**
     * Notify scanner receivers of a scanner state change.
     *
     * @param newState the new connection state.
     */
    private fun notifyOnScannerStateChange(newState: DeviceConnectionStatus) {
        log("received scannerstate $newState")
        scannerReceivers.forEach {
            it.onScannerStateChanged(newState)
        }
    }

    /**
     * Notify scanner receivers when the default scan feedback changed enabled/disabled state.
     *
     * @param defaultScanFeedback new state.
     */
    private fun notifyOnDefaultScanFeedbackChanged(defaultScanFeedback: Boolean) {
        log("received newDefaultScanFeedback $defaultScanFeedback")
        scannerReceivers.forEach {
            it.defaultFeedbackEnabled = defaultScanFeedback
        }
    }

    /**
     * A small short-cut function for sending broadcasts.
     *
     * @param intent intent.
     */
    private fun sendBroadcast(intent: Intent) {
        context.sendBroadcast(intent)
    }

    /**
     * A small logging function.
     *
     * @param message your log text.
     */
    private fun log(message: String) {
        Log.d(TAG, message)
    }

    /**
     * Trigger feedback on a scanner.
     *
     * @param feedbackSequenceId desired feedback sequence ID.
     */
    fun triggerFeedback(feedbackSequenceId: Int) {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_FEEDBACK_PLAY_SEQUENCE_INTENT
            it.putExtra(ApiConstants.EXTRA_FEEDBACK_SEQUENCE_ID, feedbackSequenceId)
        }
        sendBroadcast(intent)
    }

    /**
     * Change the default scan feedback.
     *
     * @param defaultScanFeedback new state.
     */
    fun updateScannerConfig(defaultScanFeedback: Boolean) {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_SCANNER_SET_CONFIG
            val bundle = Bundle().apply {
                putBoolean(ApiConstants.EXTRA_CONFIG_DISABLE_SCAN_FEEDBACK_STRING, defaultScanFeedback)
            }
            it.putExtra(ApiConstants.EXTRA_CONFIG_BUNDLE, bundle)
        }
        sendBroadcast(intent)
    }

    companion object {

        private const val TAG = "IntentApiApp:MsgHandler"
    }
}