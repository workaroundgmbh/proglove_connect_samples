package de.proglove.example.intent

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.proglove.example.intent.enums.DeviceConnectionStatus
import de.proglove.example.intent.enums.DisplayConnectionStatus
import de.proglove.example.intent.enums.ScannerConnectionStatus
import de.proglove.example.intent.interfaces.IIntentDisplayOutput
import de.proglove.example.intent.interfaces.IIntentScannerOutput
import kotlinx.android.synthetic.main.activity_intent.*
import kotlinx.android.synthetic.main.feedback_selection_layout.*
import java.text.DateFormat
import java.util.*

/**
 * PG Intent API usage example with a scanner and a display.
 */
class IntentActivity : AppCompatActivity(), IIntentDisplayOutput, IIntentScannerOutput {

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

        getScannerStateBtn.setOnClickListener {
            messageHandler.requestScannerState()
        }
        triggerFeedbackButton.setOnClickListener {
            val selectedFeedbackId = getFeedbackId()
            messageHandler.triggerFeedback(selectedFeedbackId)
        }
        //setting first Item as selected by default
        radioGroup.check(feedbackId1RB.id)

        defaultFeedbackSwitch.setOnClickListener {
            val defaultScanFeedback = defaultFeedbackSwitch.isChecked
            messageHandler.updateScannerConfig(defaultScanFeedback)
        }

        disconnectD3Btn.setOnClickListener {
            messageHandler.sendDisconnectD3()
        }

        getDisplayState.setOnClickListener {
            messageHandler.requestDisplayState()
        }

        sendTestScreenD3Btn.setOnClickListener {
            messageHandler.sendTestScreen(
                "PG1",
                "1|Bezeichnung|Kopfairbag|2|Fahrzeug-Typ|Hatchback|3|Teilenummer|K867 86 027 H3",
                "|"
            )
        }

        sendTestScreenD3Btn2.setOnClickListener {
            messageHandler.sendTestScreen(
                "PG2",
                "1|Stück|1 Pk|2|Bezeichnung|Gemüsemischung|3|Stück|420|4|Bezeichnung|Früchte Müsli|5|Stück|30|6|Bezeichnung|Gebäck-Stangen",
                "|"
            )
        }

        sendTestScreenD3BtnFailing.setOnClickListener {
            messageHandler.sendTestScreen("PG2", "1|Header1|Content1|8|Header2|Content2", "|")
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
            symbology?.let { s ->
                lastSymbologyOutput.text = s
            }
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

    companion object {

        const val TAG = "PGIntentActivity"
    }
}