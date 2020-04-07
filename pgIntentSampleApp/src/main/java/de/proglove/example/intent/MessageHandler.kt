package de.proglove.example.intent

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
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
import java.util.Collections

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
        handleNewIntent(intent)
    }

    /**
     * Parses new Intent and notifies [scannerReceivers] or [displayReceivers] on which action and data is received.
     */
    fun handleNewIntent(intent: Intent?) {
        intent?.let {
            // match actions specified in the intent filer and extract relevant data accordingly
            when (it.action) {
                ApiConstants.ACTION_BARCODE_INTENT,
                ApiConstants.ACTION_BARCODE_VIA_START_ACTIVITY_INTENT -> {
                    handleScannedBarcode(it)
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
                                bundle.getBoolean(ApiConstants.EXTRA_CONFIG_DEFAULT_SCAN_FEEDBACK_ENABLED)
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
                    if (intent.hasExtra(ApiConstants.EXTRA_DATA_STRING_PG) || intent.hasExtra(ApiConstants.EXTRA_SYMBOLOGY_STRING_PG)) {
                        handleScannedBarcode(it)
                    } else {
                        log("Unrecognized Intent")
                    }
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
     * Request current scanner state to be broadcast and sent down to the registered scanner callback output.
     */
    fun requestScannerState() {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_GET_STATE_INTENT
        }
        sendBroadcast(intent)
    }

    /**
     * Request current display state to be broadcast and sent down to the registered display callback output.
     */
    fun requestDisplayState() {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_GET_DISPLAY_STATE_INTENT
        }
        sendBroadcast(intent)
    }

    /**
     * Disconnect display.
     */
    fun sendDisconnectDisplay() {
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
     * @param durationMs the duration for which this screen should be displayed. 0 means indefinite display
     * @param refreshType the refresh type to be used for this screen setting.
     *      Valid values are: ["DEFAULT", "FULL_REFRESH", "PARTIAL_REFRESH"]
     */
    fun sendTestScreen(id: String, content: String, separator: String, durationMs: Int = 0, refreshType: String? = null) {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_SET_SCREEN_INTENT
            it.putExtra(ApiConstants.EXTRA_DISPLAY_TEMPLATE_ID, id)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_DATA, content)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_SEPARATOR, separator)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_DURATION, durationMs)
            refreshType?.let { refreshType ->
                it.putExtra(ApiConstants.EXTRA_DISPLAY_REFRESH_TYPE, refreshType)
            }
        }
        sendBroadcast(intent)
    }

    /**
     * Gets scanned barcode data received [Intent] and notifies [scannerReceivers].
     */
    private fun handleScannedBarcode(intent: Intent) {
        val barcodeContent = intent.getStringExtra(ApiConstants.EXTRA_DATA_STRING_PG)
        val symbology = intent.getStringExtra(ApiConstants.EXTRA_SYMBOLOGY_STRING_PG)
        barcodeContent?.let { s ->
            log("received Barcode pg: $s")
            notifyOnReceivedBarcode(s, symbology)
        }
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
        log("received scannerState $newState")
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
     * @param shouldReplaceQueue (optional) If true all currently queued up commands are canceled and only this command
     *  will be enqueued
     */
    fun triggerFeedback(feedbackSequenceId: Int, shouldReplaceQueue: Boolean = false) {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_FEEDBACK_PLAY_SEQUENCE_INTENT
            it.putExtra(ApiConstants.EXTRA_FEEDBACK_SEQUENCE_ID, feedbackSequenceId)
            it.putExtra(ApiConstants.EXTRA_REPLACE_QUEUE, shouldReplaceQueue)
        }
        sendBroadcast(intent)
    }

    /**
     * Change the default scan feedback.
     *
     * @param isDefaultScanFeedbackEnabled new state.
     */
    fun updateScannerConfig(isDefaultScanFeedbackEnabled: Boolean) {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_SCANNER_SET_CONFIG
            val bundle = Bundle().apply {
                putBoolean(ApiConstants.EXTRA_CONFIG_DEFAULT_SCAN_FEEDBACK_ENABLED, isDefaultScanFeedbackEnabled)
            }
            it.putExtra(ApiConstants.EXTRA_CONFIG_BUNDLE, bundle)
        }
        sendBroadcast(intent)
    }

    fun showPickDisplayOrientationDialog() {
        val intent = Intent()

        intent.component = ComponentName(
                ApiConstants.DISPLAY_ORIENTATION_ACTIVITY_PACKAGE_NAME,
                ApiConstants.DISPLAY_ORIENTATION_ACTIVITY_CLASS_NAME
        )

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            log("Error: No or wrong version of PgConnect!")
        }
    }

    /**
     * Changes configuration profile.
     *
     * @param profileId profiles unique id.
     */
    fun changeConfigProfile(profileId: String) {
        val intent = Intent().apply {
            action = ApiConstants.ACTION_CHANGE_CONFIG_PROFILE
            putExtra(ApiConstants.EXTRA_CONFIG_PROFILE_ID, profileId)
        }
        sendBroadcast(intent)
    }

    companion object {

        private const val TAG = "IntentApiApp:MsgHandler"
    }
}