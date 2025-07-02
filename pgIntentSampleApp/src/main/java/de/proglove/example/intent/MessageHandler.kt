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
import de.proglove.example.intent.enums.DisplayDeviceType
import de.proglove.example.intent.interfaces.IIntentDisplayOutput
import de.proglove.example.intent.interfaces.IIntentScannerOutput
import de.proglove.example.intent.interfaces.IScannerConfigurationChangeOutput
import de.proglove.example.intent.interfaces.IStatusOutput
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
        it.addAction(ApiConstants.ACTION_BARCODE_INTENT_IVANTI)
        it.addAction(ApiConstants.ACTION_SCANNER_STATE_INTENT)
        it.addAction(ApiConstants.ACTION_DISPLAY_STATE_INTENT)
        it.addAction(ApiConstants.ACTION_BUTTON_PRESSED_INTENT)
        it.addAction(ApiConstants.ACTION_SET_SCREEN_RESULT_INTENT)
        it.addAction(ApiConstants.ACTION_TRIGGER_UNBLOCKED_INTENT)
        it.addAction(ApiConstants.ACTION_CONFIG_PROFILES)
        it.addAction(ApiConstants.ACTION_SCANNER_CONFIG_CHANGE)
        it.addAction(ApiConstants.ACTION_RECEIVE_DEVICE_VISIBILITY_INFO)
        it.addAction(ApiConstants.ACTION_DISPLAY_DEVICE_TYPE_INTENT)
        it.addAction(ApiConstants.ACTION_SET_DISPLAY_SCREEN_V2_RESULT_INTENT)
        it.addAction(ApiConstants.ACTION_DISPLAY_SCREEN_EVENT_INTENT)
        it.addCategory(Intent.CATEGORY_DEFAULT)
    }

    private var statusListener: IStatusOutput? = null
    private var scannerConfigurationChangeListener: IScannerConfigurationChangeOutput? = null

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
                ApiConstants.ACTION_BARCODE_INTENT_IVANTI -> {
                    log("got ACTION_BARCODE_INTENT_IVANTI")
                    handleIvantiBarcode(it)
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

                ApiConstants.ACTION_SCANNER_CONFIG_CHANGE -> {
                    log("got ACTION_SCANNER_CONFIG_CHANGE")
                    val statusCode = intent.getStringExtra(ApiConstants.EXTRA_SCANNER_CONFIG_CHANGE_STATUS)
                            ?: "No scanner config status"
                    val errorMessage: String? = intent.getStringExtra(ApiConstants.EXTRA_SCANNER_CONFIG_CHANGE_ERROR_TEXT)
                    scannerConfigurationChangeListener?.onScannerConfigurationChange(statusCode, errorMessage)
                    notifyOnScannerConfigurationChange(statusCode, errorMessage)
                }

                ApiConstants.ACTION_DISPLAY_STATE_INTENT -> {
                    log("got ACTION_DISPLAY_STATE_INTENT")
                    intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_STATE)?.let { s ->
                        log("got display $s")
                        notifyOnDisplayStateChange(DeviceConnectionStatus.valueOf(s))
                    }
                }
                ApiConstants.ACTION_BUTTON_PRESSED_INTENT -> {
                    log("got ACTION_BUTTON_PRESSED_INTENT")
                    val buttonId = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_BUTTON)
                    val screenContext = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SCREEN_CONTEXT)

                    notifyOnButtonPressed(buttonId ?: "", screenContext ?: "")
                }
                ApiConstants.ACTION_SET_SCREEN_RESULT_INTENT -> {
                    log("got ACTION_SET_SCREEN_RESULT_INTENT")
                    val success = intent.getBooleanExtra(ApiConstants.EXTRA_DISPLAY_SET_SCREEN_SUCCESS, false)
                    var errorMessage: String? = null
                    if (!success) {
                        errorMessage = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SET_SCREEN_ERROR_TEXT)
                        statusListener?.onStatusReceived("set screen error")
                    } else {
                        statusListener?.onStatusReceived("set screen success")
                    }
                    notifyOnSetScreenSuccess(success, errorMessage)
                }
                ApiConstants.ACTION_TRIGGER_UNBLOCKED_INTENT -> {
                    log("got ACTION_TRIGGER_UNBLOCKED_INTENT")
                    notifyOnTriggerUnblocked()
                }
                ApiConstants.ACTION_CONFIG_PROFILES -> {
                    log("got ACTION_CONFIG_PROFILES")
                    val configProfilesIds: Array<String> = intent.getStringArrayExtra(ApiConstants.EXTRA_CONFIG_PROFILE_ID)
                            ?: emptyArray()
                    val activeProfileId = intent.getStringExtra(ApiConstants.EXTRA_CONFIG_PROFILE_ACTIVE_ID)
                            ?: ""

                    notifyOnConfigProfilesReceived(configProfilesIds, activeProfileId)
                }
                ApiConstants.ACTION_RECEIVE_DEVICE_VISIBILITY_INFO -> {
                    log("got ACTION_RECEIVE_DEVICE_VISIBILITY_INFO")
                    notifyOnDeviceVisibilityInfoReceived(intent)
                }
                ApiConstants.ACTION_DISPLAY_DEVICE_TYPE_INTENT -> {
                    log("got ACTION_DISPLAY_DEVICE_TYPE_INTENT")
                    intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_DEVICE_TYPE)?.let { s ->
                        log("got display type $s")
                        notifyOnDisplayTypeChanged(DisplayDeviceType.valueOf(s))
                    }
                }
                ApiConstants.ACTION_SET_DISPLAY_SCREEN_V2_RESULT_INTENT -> {
                    log("got ACTION_SET_DISPLAY_SCREEN_V2_RESULT_INTENT")
                    val success = intent.getBooleanExtra(ApiConstants.EXTRA_DISPLAY_SET_SCREEN_SUCCESS, false)
                    var errorMessage: String? = null
                    if (!success) {
                        errorMessage = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SET_SCREEN_ERROR_TEXT)
                        statusListener?.onStatusReceived("set screen v2 error")
                    } else {
                        statusListener?.onStatusReceived("set screen v2 success")
                    }
                    notifyOnSetScreenSuccess(success, errorMessage)
                }
                ApiConstants.ACTION_DISPLAY_SCREEN_EVENT_INTENT -> {
                    log("got ACTION_DISPLAY_SCREEN_EVENT_INTENT")
                    val screenContext = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SCREEN_CONTEXT)?.also { s ->
                        log("got display screen context $s")
                    } ?: ""

                    val screenEvent = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SCREEN_EVENT)?.also { s ->
                        log("got display screen event $s")
                    } ?: ""

                    notifyOnDisplayEventReceived(screenEvent, screenContext)
                }
                else -> {
                    if (intent.hasExtra(ApiConstants.EXTRA_DATA_STRING_PG) || intent.hasExtra(ApiConstants.EXTRA_SYMBOLOGY_STRING_PG)) {
                        handleScannedBarcode(it)
                    } else {
                        log("Unrecognized Intent: $it")
                    }
                }
            }
        }
    }

    fun setStatusListener(statusListener: IStatusOutput) {
        this.statusListener = statusListener
    }

    fun setScannerConfigurationChangeListener(scannerConfigurationChangeListener: IScannerConfigurationChangeOutput) {
        this.scannerConfigurationChangeListener = scannerConfigurationChangeListener
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
     * Request current display device type to be broadcast and sent down to the registered display callback output.
     */
    fun requestDisplayDeviceType() {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_GET_DISPLAY_DEVICE_TYPE_INTENT
        }
        sendBroadcast(intent)
    }

    /**
     * Connect scanner or display.
     */
    fun sendConnect() {
        val intent = Intent().also {
            it.component = ComponentName(
                PAIRING_ACTIVITY_PACKAGE_NAME,
                PAIRING_ACTIVITY_CLASS_NAME
            )
        }
        try {
            // Make sure to use the activity context here and not the application context.
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Unable to start PairingActivity, is Insight Mobile installed?", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Disconnect scanner.
     */
    fun sendDisconnectScanner() {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_DISCONNECT_INTENT
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
     * @param rightHeaders additional data for display to be shown in upper right corner(s) of the field(s).
     * @param separator separator.
     * @param durationMs the duration for which this screen should be displayed. 0 means indefinite display
     * @param refreshType the refresh type to be used for this screen setting.
     *      Valid values are: ["DEFAULT", "FULL_REFRESH", "PARTIAL_REFRESH"]
     */
    fun sendTestScreen(
        id: String,
        content: String,
        rightHeaders: String?,
        separator: String,
        durationMs: Int = 0,
        refreshType: String? = null
    ) {
        val intent = Intent().also {
            it.action = ApiConstants.ACTION_SET_SCREEN_INTENT
            it.putExtra(ApiConstants.EXTRA_DISPLAY_TEMPLATE_ID, id)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_DATA, content)
            rightHeaders?.let { rightHeaders ->
                it.putExtra(ApiConstants.EXTRA_DISPLAY_RIGHT_HEADERS, rightHeaders)
            }
            it.putExtra(ApiConstants.EXTRA_DISPLAY_SEPARATOR, separator)
            it.putExtra(ApiConstants.EXTRA_DISPLAY_DURATION, durationMs)
            refreshType?.let { refreshType ->
                it.putExtra(ApiConstants.EXTRA_DISPLAY_REFRESH_TYPE, refreshType)
            }
        }
        sendBroadcast(intent)
    }

    /**
     * Gets scanned barcode data from received [Intent] and notifies [scannerReceivers].
     */
    private fun handleScannedBarcode(intent: Intent) {
        val barcodeContent = intent.getStringExtra(ApiConstants.EXTRA_DATA_STRING_PG)
        val symbology = intent.getStringExtra(ApiConstants.EXTRA_SYMBOLOGY_STRING_PG) ?: ""
        val screenContext = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SCREEN_CONTEXT) ?: ""

        barcodeContent?.let { s ->
            log("received Barcode pg: $s")
            notifyOnReceivedBarcode(s, symbology, screenContext)
        }
    }

    /**
     * Gets barcode data and button ID from received [Intent] and notifies [scannerReceivers].
     */
    private fun handleIvantiBarcode(intent: Intent) {
        val barcodeContent = intent.getStringExtra(ApiConstants.EXTRA_DATA_STRING_PG)
        val symbology = intent.getStringExtra(ApiConstants.EXTRA_SYMBOLOGY_STRING_PG) ?: ""
        val screenContext = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_SCREEN_CONTEXT) ?: ""

        // When configured, Ivanti barcode intent is triggered by double click
        // on a connected device. In this case button ID is added as extra
        val buttonId = intent.getStringExtra(ApiConstants.EXTRA_DISPLAY_BUTTON)

        barcodeContent?.let { s ->
            log("received Ivanti Barcode: $s")
            notifyOnReceivedBarcode(s, symbology, screenContext)
        }

        buttonId?.let {
            log("button ID attached to Ivanti Barcode: $buttonId")
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

    private fun notifyOnTriggerUnblocked() {
        Toast.makeText(context, "Triggers unblocked", Toast.LENGTH_LONG).show()
    }

    /**
     * Notify display receivers of a button press on D3.
     */
    private fun notifyOnButtonPressed(buttonId: String, screenContext: String) {
        log("ButtonId: $buttonId")

        displayReceivers.forEach {
            it.onButtonPressed(buttonId, screenContext)
        }
    }

    /**
     * Notify display receivers of a D3 state change.
     *
     * @param newState the new connection state.
     */
    private fun notifyOnDisplayStateChange(newState: DeviceConnectionStatus) {
        log("received displayState $newState")
        displayReceivers.forEach {
            it.onDisplayStateChanged(newState)
        }
    }

    /**
     * Notify display receivers of a device type change.
     *
     * @param displayDeviceType the new display device type.
     */
    private fun notifyOnDisplayTypeChanged(displayDeviceType: DisplayDeviceType) {
        log("received displayDeviceType $displayDeviceType")
        displayReceivers.forEach {
            it.onDisplayDeviceTypeChanged(displayDeviceType)
        }
    }

    private fun notifyOnDeviceVisibilityInfoReceived(intent: Intent) {
        val serialNumber =
                intent.getStringExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_SERIAL_NUMBER) ?: ""
        val firmwareRevision =
                intent.getStringExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_FIRMWARE_REVISION)
                        ?: ""
        val batteryLevel =
                intent.getIntExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_BATTERY_LEVEL, 0)
        val bceRevision =
                intent.getStringExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_BCE_REVISION) ?: ""
        val modelNumber =
                intent.getStringExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_MODEL_NUMBER) ?: ""
        val manufacturer =
                intent.getStringExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_MANUFACTURER) ?: ""
        val appVersion =
                intent.getStringExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_APP_VERSION) ?: ""
        val deviceBluetoothMacAddress =
                intent.getStringExtra(ApiConstants.EXTRA_DEVICE_VISIBILITY_INFO_DEVICE_BLUETOOTH_MAC_ADDRESS) ?: ""

        scannerReceivers.forEach {
            it.onDeviceVisibilityInfoReceived(
                    serialNumber,
                    firmwareRevision,
                    batteryLevel,
                    bceRevision,
                    modelNumber,
                    manufacturer,
                    deviceBluetoothMacAddress,
                    appVersion)
        }
    }

    /**
     * Notify scanner receivers when a barcode scan has been received.
     *
     * @param value scanned barcode string.
     * @param symbology symbology of that barcode, if supported.
     */
    private fun notifyOnReceivedBarcode(value: String, symbology: String, screenContext: String) {
        log("notify on Barcode $value")

        scannerReceivers.forEach {
            it.onBarcodeScanned(value, symbology, screenContext)
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

    private fun notifyOnConfigProfilesReceived(profileIds: Array<String>, activeProfileId: String) {
        log("received ${profileIds.size} config profiles with \"$activeProfileId\" as the active one")
        scannerReceivers.forEach {
            it.onConfigProfilesReceived(profileIds, activeProfileId)
        }
    }

    /**
     * Show a toast with scanner config change result and refresh the active configuration details.
     *
     * @param success did it succeed
     * @param errorMessage error message, if did not succeed.
     */
    private fun notifyOnScannerConfigurationChange(status: String, errorMessage: String?) {
        Toast.makeText(context, "Set scanner configuration status: $status, error message: $errorMessage", Toast.LENGTH_LONG).show()
    }

    /**
     * Notify display receivers of a display event.
     *
     * @param event the display event.
     */
    private fun notifyOnDisplayEventReceived(event: String, context: String) {
        log("received display event: $event")
        displayReceivers.forEach {
            it.onDisplayEventReceived(event, context)
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

    fun getActiveConfigProfile() {
        val intent = Intent().apply {
            action = ApiConstants.ACTION_GET_CONFIG_PROFILES
        }
        sendBroadcast(intent)
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

    /**
     * Blocks (default) trigger indefinitely.
     */
    fun blockTrigger() {
        val intent = Intent().apply {
            action = ApiConstants.ACTION_BLOCK_TRIGGER
            putExtra(ApiConstants.EXTRA_TRIGGERS_BLOCK, arrayOf("BUTTON_1_SINGLE_CLICK"))
            putExtra(ApiConstants.EXTRA_TRIGGERS_UNBLOCK_BY, arrayOf("BUTTON_1_DOUBLE_CLICK"))
        }
        sendBroadcast(intent)
    }

    /**
     * Blocks all triggers for 10 seconds.
     * Requires Insight Mobile v1.13.0+ and Scanner v2.5.0+
     */
    fun blockAllTriggersFor10s() {
        val intent = Intent().apply {
            action = ApiConstants.ACTION_BLOCK_TRIGGER
            putExtra(ApiConstants.EXTRA_TRIGGERS_BLOCK, emptyArray<String>())
            putExtra(ApiConstants.EXTRA_TRIGGERS_UNBLOCK_BY, emptyArray<String>())
            putExtra(ApiConstants.EXTRA_DISPLAY_DURATION, 10000)
        }
        sendBroadcast(intent)
    }

    /**
     * Unblocks (all) triggers.
     */
    fun unblockTrigger() {
        val intent = Intent().apply {
            action = ApiConstants.ACTION_UNBLOCK_TRIGGER
        }
        sendBroadcast(intent)
    }

    /**
     * NOTE: In order to receive device visibility info, you need to have valid ProGlove License
     * imported in the Insight Mobile.
     * For more info reach out to your contact person at ProGlove.
     */
    fun obtainDeviceVisibility() {
        val intent = Intent().apply {
            action = ApiConstants.ACTION_OBTAIN_DEVICE_VISIBILITY_INFO
        }
        sendBroadcast(intent)
    }

    fun sendPgNtfT5() {
        sendBroadcast(DisplayV2Examples.PgNtfT5)
    }

    fun sendPgWork3Btn2T1() {
        sendBroadcast(DisplayV2Examples.PgWork3Btn2T1)
    }

    fun sendPgListT1() {
        sendBroadcast(DisplayV2Examples.PgListT1)
    }

    fun sendTimerScreen() {
        sendBroadcast(DisplayV2Examples.TimerScreen)
    }

    fun updateGoals(totalStepsGoal: Int, totalScansGoal: Int, averageScantimeGoal: Float) {
        val intent = Intent().apply {
            action = ApiConstants.ACTION_CONFIGURE_ACTIVITY_GOALS
            putExtra(ApiConstants.EXTRA_ACTIVITY_GOAL_TOTAL_STEPS, totalStepsGoal)
            putExtra(ApiConstants.EXTRA_ACTIVITY_GOAL_TOTAL_SCANS, totalScansGoal)
            putExtra(ApiConstants.EXTRA_ACTIVITY_GOAL_AVERAGE_SCAN_SPEED, averageScantimeGoal)
        }
        sendBroadcast(intent)
    }

    companion object {

        private const val TAG = "IntentApiApp:MsgHandler"

        private const val PAIRING_ACTIVITY_PACKAGE_NAME = "de.proglove.connect"
        private const val PAIRING_ACTIVITY_CLASS_NAME = "de.proglove.coreui.activities.PairingActivity"
    }
}