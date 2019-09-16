package sdk.scanner.proglove.de.proglovescannersdk

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.proglove.sdk.ConnectionStatus
import de.proglove.sdk.utils.Logger
import kotlinx.android.synthetic.main.activity_scanner_intent.defaultFeedbackSwitch
import kotlinx.android.synthetic.main.activity_scanner_intent.getScannerStateBtn
import kotlinx.android.synthetic.main.activity_scanner_intent.intentInputField
import kotlinx.android.synthetic.main.activity_scanner_intent.lastContactOutput
import kotlinx.android.synthetic.main.activity_scanner_intent.lastSymbologyOutput
import kotlinx.android.synthetic.main.activity_scanner_intent.scannerstateOutput
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId1RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId2RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId3RB
import kotlinx.android.synthetic.main.feedback_selection_layout.radioGroup
import kotlinx.android.synthetic.main.feedback_selection_layout.triggerFeedbackButton
import java.text.DateFormat
import java.util.Date

class ScannerIntentActivity : AppCompatActivity(), IIntentScannerOutput {

    private var scannerConnectionState = ScannerConnection.DISCONNECTED
    private val messageHandler: MessageHandler = MessageHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner_intent)

        registerReceiver(messageHandler, messageHandler.filter)
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
    }

    private fun getFeedbackId(): Int {
        return when (radioGroup.checkedRadioButtonId) {
            feedbackId1RB.id -> 1
            feedbackId2RB.id -> 2
            feedbackId3RB.id -> 3
            //returning 1 as default
            else -> 1
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(messageHandler)
        messageHandler.unregisterScannerOutput(this)
    }

    private fun updateConnectionLabel() {
        runOnUiThread {
            if (scannerConnectionState == ScannerConnection.CONNECTED) {
                scannerstateOutput.setText(R.string.scanner_connected)
            } else if (scannerConnectionState == ScannerConnection.DISCONNECTED) {
                scannerstateOutput.setText(R.string.scanner_disconnected)
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

    override fun onScannerStateChanged(status: ConnectionStatus) {
        Logger.d("Did receive scanner status: $status")
        scannerConnectionState = when (status) {
            ConnectionStatus.CONNECTED -> {
                ScannerConnection.CONNECTED
            }
            ConnectionStatus.DISCONNECTED -> {
                ScannerConnection.DISCONNECTED
            }
            else -> {
                ScannerConnection.CONNECTING
            }
        }
        updateConnectionLabel()
        updateLastContact()
    }

    override fun onScannerDefaultFeedbackUpdated(defaultFeedbackEnabled: Boolean) {
        runOnUiThread {
            defaultFeedbackSwitch.isChecked = defaultFeedbackEnabled
        }
    }
}

interface IIntentScannerOutput {

    /**
     *  Callback Method that is called for each scanned barcode
     *  It will only be called once per Barcode
     *
     *  @param barcode the scanned barcode as a string representation, based on the currently configured scanner settings.
     */
    fun onBarcodeScanned(barcode: String, symbology: String?)

    /**
     * Much more detailed callbacks for the scanner status
     *
     * To understand the current state (searching for devices, time outs, etc) subscribe to this optional method.
     */
    fun onScannerStateChanged(status: ConnectionStatus)


    /**
     * set the default scan feedback
     */
    fun onScannerDefaultFeedbackUpdated(defaultFeedbackEnabled: Boolean)
}