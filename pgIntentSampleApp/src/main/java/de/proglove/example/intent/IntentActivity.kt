package de.proglove.example.intent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.proglove.example.common.DisplaySampleData
import de.proglove.example.intent.enums.DeviceConnectionStatus
import de.proglove.example.intent.enums.DisplayConnectionStatus
import de.proglove.example.intent.enums.ScannerConnectionStatus
import de.proglove.example.intent.interfaces.IIntentDisplayOutput
import de.proglove.example.intent.interfaces.IIntentScannerOutput
import de.proglove.example.intent.interfaces.IStatusOutput
import kotlinx.android.synthetic.main.activity_intent.altCustomProfileButton
import kotlinx.android.synthetic.main.activity_intent.customProfileButton
import kotlinx.android.synthetic.main.activity_intent.defaultFeedbackSwitch
import kotlinx.android.synthetic.main.activity_intent.defaultProfileButton
import kotlinx.android.synthetic.main.activity_intent.disconnectDisplayBtn
import kotlinx.android.synthetic.main.activity_intent.displayStateOutput
import kotlinx.android.synthetic.main.activity_intent.getDisplayState
import kotlinx.android.synthetic.main.activity_intent.getScannerStateBtn
import kotlinx.android.synthetic.main.activity_intent.intentInputField
import kotlinx.android.synthetic.main.activity_intent.lastContactOutput
import kotlinx.android.synthetic.main.activity_intent.lastResponseValue
import kotlinx.android.synthetic.main.activity_intent.lastSymbologyOutput
import kotlinx.android.synthetic.main.activity_intent.pickDisplayOrientationDialogBtn
import kotlinx.android.synthetic.main.activity_intent.scannerStateOutput
import kotlinx.android.synthetic.main.activity_intent.sendFeedbackWithReplaceQueueSwitch
import kotlinx.android.synthetic.main.activity_intent.sendNotificationTestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPartialRefreshTestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPg1ATestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendPg1TestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendTestScreenBtn
import kotlinx.android.synthetic.main.activity_intent.sendTestScreenBtnFailing
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId1RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId2RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId3RB
import kotlinx.android.synthetic.main.feedback_selection_layout.radioGroup
import kotlinx.android.synthetic.main.feedback_selection_layout.triggerFeedbackButton
import java.text.DateFormat
import java.util.Date

/**
 * PG Intent API usage example with a scanner and a display.
 */
class IntentActivity : AppCompatActivity(), IIntentDisplayOutput, IIntentScannerOutput, IStatusOutput {

    override var defaultFeedbackEnabled: Boolean
        get() = defaultFeedbackSwitch.isChecked
        set(value) {
            defaultFeedbackSwitch.isChecked = value
        }

    private var scannerConnectionState = ScannerConnectionStatus.DISCONNECTED
    private var displayConnectionState = DisplayConnectionStatus.DISCONNECTED
    private val messageHandler: MessageHandler = MessageHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intent)

        registerReceiver(messageHandler, messageHandler.filter)
        messageHandler.registerDisplayOutput(this)
        messageHandler.registerScannerOutput(this)
        messageHandler.setStatusListener(this)

        // Handle intent sent with start activity action which created this activity.
        // That Intent will not trigger #onNewIntent.
        messageHandler.handleNewIntent(intent)

        getScannerStateBtn.setOnClickListener {
            messageHandler.requestScannerState()
        }
        triggerFeedbackButton.setOnClickListener {
            val selectedFeedbackId = getFeedbackId()
            val shouldReplaceQueue = sendFeedbackWithReplaceQueueSwitch.isChecked
            messageHandler.triggerFeedback(selectedFeedbackId, shouldReplaceQueue)
        }
        //setting first Item as selected by default
        radioGroup.check(feedbackId1RB.id)

        defaultFeedbackSwitch.setOnClickListener {
            val defaultScanFeedback = defaultFeedbackSwitch.isChecked
            messageHandler.updateScannerConfig(defaultScanFeedback)
        }

        defaultProfileButton.setOnClickListener {
            messageHandler.changeConfigProfile("profile0")
        }

        customProfileButton.setOnClickListener {
            messageHandler.changeConfigProfile("profile1")
        }

        altCustomProfileButton.setOnClickListener {
            messageHandler.changeConfigProfile("profile2")
        }

        disconnectDisplayBtn.setOnClickListener {
            messageHandler.sendDisconnectDisplay()
        }

        getDisplayState.setOnClickListener {
            messageHandler.requestDisplayState()
        }

        sendTestScreenBtn.setOnClickListener {
            val templateId = "PG2"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, separator)
        }

        sendPartialRefreshTestScreenBtn.setOnClickListener {
            val templateId = "PG3"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, separator, 0, "PARTIAL_REFRESH")
        }

        sendNotificationTestScreenBtn.setOnClickListener {
            val templateId = "PG2I"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, separator, 3000)
        }

        sendPg1TestScreenBtn.setOnClickListener {
            val templateId = "PG1"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, separator, 3000)
        }

        sendPg1ATestScreenBtn.setOnClickListener {
            val templateId = "PG1A"
            val separator = "|"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                "${index + 1}$separator${pair.first}$separator${pair.second.random()}"
            }.joinToString(separator)
            messageHandler.sendTestScreen(templateId, templateFields, separator, 3000)
        }

        sendTestScreenBtnFailing.setOnClickListener {
            messageHandler.sendTestScreen("PG2", "|||", ";")
        }

        pickDisplayOrientationDialogBtn.setOnClickListener {
            messageHandler.showPickDisplayOrientationDialog()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        messageHandler.handleNewIntent(intent)
    }

    private fun getSampleDataForTemplate(template: String): List<Pair<String, Array<String>>> {
        return when (template) {
            "PG2" -> listOf(
                    DisplaySampleData.SAMPLE_STORAGE_UNIT,
                    DisplaySampleData.SAMPLE_DESTINATION
            )
            "PG3" -> listOf(
                    DisplaySampleData.SAMPLE_STORAGE_UNIT,
                    DisplaySampleData.SAMPLE_ITEM,
                    DisplaySampleData.SAMPLE_QUANTITY
            )
            "PG1I" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG1E" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG1C" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG2I" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG2E" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG2C" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG1" -> listOf(arrayOf(DisplaySampleData.SAMPLE_MESSAGES, DisplaySampleData.SAMPLE_MESSAGES_2, DisplaySampleData.SAMPLE_ITEM).random())
            "PG1A" -> listOf(DisplaySampleData.SAMPLE_MESSAGES_NO_HEADER)
            else -> listOf()
        }
    }

    private fun getFeedbackId() = when (radioGroup.checkedRadioButtonId) {
        feedbackId1RB.id -> 1
        feedbackId2RB.id -> 2
        feedbackId3RB.id -> 3
        // returning 1 as default
        else -> 1
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(messageHandler)
        messageHandler.unregisterDisplayOutput(this)
        messageHandler.unregisterScannerOutput(this)
    }

    private fun updateConnectionLabel() {
        runOnUiThread {
            if (scannerConnectionState == ScannerConnectionStatus.CONNECTED) {
                scannerStateOutput.setText(R.string.scanner_connected)
            } else if (scannerConnectionState == ScannerConnectionStatus.DISCONNECTED) {
                scannerStateOutput.setText(R.string.scanner_disconnected)
            }

            if (displayConnectionState == DisplayConnectionStatus.CONNECTED) {
                displayStateOutput.setText(R.string.display_connected)
            } else if (displayConnectionState == DisplayConnectionStatus.DISCONNECTED) {
                displayStateOutput.setText(R.string.display_disconnected)
            }
        }

        updateLastContact()
    }

    private fun updateLastContact() {
        val date = Date()
        val dateFormat = DateFormat.getDateTimeInstance()
        val formattedDate = dateFormat.format(date)
        runOnUiThread {
            lastContactOutput.text = formattedDate
        }
    }

    override fun onBarcodeScanned(barcode: String, symbology: String?) {
        runOnUiThread {
            intentInputField?.text = barcode
            Toast.makeText(this, "Got barcode: $barcode", Toast.LENGTH_LONG).show()
            lastSymbologyOutput.text = symbology ?: ""
        }
        updateLastContact()
    }

    override fun onScannerStateChanged(status: DeviceConnectionStatus) {
        Log.i(TAG, "Did receive scanner status: $status")
        scannerConnectionState = when (status) {
            DeviceConnectionStatus.CONNECTED -> {
                ScannerConnectionStatus.CONNECTED
            }
            DeviceConnectionStatus.DISCONNECTED -> {
                ScannerConnectionStatus.DISCONNECTED
            }
            else -> {
                ScannerConnectionStatus.CONNECTING
            }
        }
        updateConnectionLabel()
        updateLastContact()
    }

    override fun onButtonPressed(buttonId: String) {
        Toast.makeText(this, "Button $buttonId pressed", Toast.LENGTH_SHORT).show()
    }

    override fun onDisplayStateChanged(status: DeviceConnectionStatus) {
        Log.i(TAG, "Did receive display status: $status")
        displayConnectionState = when (status) {
            DeviceConnectionStatus.CONNECTED -> {
                DisplayConnectionStatus.CONNECTED
            }
            DeviceConnectionStatus.DISCONNECTED -> {
                DisplayConnectionStatus.DISCONNECTED
            }
            else -> {
                DisplayConnectionStatus.CONNECTING
            }
        }
        updateConnectionLabel()
    }

    override fun onStatusReceived(status: String) {
        runOnUiThread {
            lastResponseValue.text = status
        }
    }

    companion object {

        const val TAG = "PGIntentActivity"
    }
}