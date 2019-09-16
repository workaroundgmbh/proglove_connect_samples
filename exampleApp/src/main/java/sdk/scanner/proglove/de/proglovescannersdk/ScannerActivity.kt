package sdk.scanner.proglove.de.proglovescannersdk

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.proglove.sdk.ConnectionStatus
import de.proglove.sdk.IServiceOutput
import de.proglove.sdk.PgError
import de.proglove.sdk.PgManager
import de.proglove.sdk.scanner.BarcodeScanResults
import de.proglove.sdk.scanner.IPgFeedbackCallback
import de.proglove.sdk.scanner.IPgImageCallback
import de.proglove.sdk.scanner.IPgScannerConfigCallback
import de.proglove.sdk.scanner.IScannerOutput
import de.proglove.sdk.scanner.ImageResolution
import de.proglove.sdk.scanner.PgImage
import de.proglove.sdk.scanner.PgImageConfig
import de.proglove.sdk.scanner.PgPredefinedFeedback
import de.proglove.sdk.scanner.PgScannerConfig
import kotlinx.android.synthetic.main.activity_main.connectScannerPinnedBtn
import kotlinx.android.synthetic.main.activity_main.connectScannerRegularBtn
import kotlinx.android.synthetic.main.activity_main.defaultFeedbackSwitch
import kotlinx.android.synthetic.main.activity_main.inputField
import kotlinx.android.synthetic.main.activity_main.serviceConnectBtn
import kotlinx.android.synthetic.main.activity_main.symbologyResult
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId1RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId2RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId3RB
import kotlinx.android.synthetic.main.feedback_selection_layout.radioGroup
import kotlinx.android.synthetic.main.feedback_selection_layout.triggerFeedbackButton
import kotlinx.android.synthetic.main.feedback_selection_layout.triggerFeedbackText
import kotlinx.android.synthetic.main.take_image_layout.imageTaken
import kotlinx.android.synthetic.main.take_image_layout.jpegQualityEditText
import kotlinx.android.synthetic.main.take_image_layout.resolutionRadioGroup
import kotlinx.android.synthetic.main.take_image_layout.takeImageButton
import kotlinx.android.synthetic.main.take_image_layout.timeoutEditText
import java.util.logging.Level
import java.util.logging.Logger

class ScannerActivity : AppCompatActivity(), IScannerOutput, IServiceOutput {

    private val logger = Logger.getLogger("sample-logger")
    private val pgManager = PgManager(logger)

    private var serviceConnectionState = ServiceConnection.DISCONNECTED
    private var scannerConnectionState = ScannerConnection.DISCONNECTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pgManager.subscribeToServiceEvents(this)
        pgManager.subscribeToScans(this)

        serviceConnectBtn.setOnClickListener {
            pgManager.ensureConnectionToService(this.applicationContext)
            updateButtonStates()
        }

        connectScannerRegularBtn.setOnClickListener {
            if (scannerConnectionState == ScannerConnection.CONNECTED) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairing()
            }
        }

        connectScannerPinnedBtn.setOnClickListener {
            if (scannerConnectionState == ScannerConnection.CONNECTED) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairingFromPinnedActivity(this)
            }
        }

        triggerFeedbackButton.setOnClickListener {
            val selectedFeedbackId = getFeedbackId()
            val feedbackText = triggerFeedbackText.text.toString()
            pgManager.triggerFeedback(
                    predefinedFeedback = selectedFeedbackId,
                    feedbackText = feedbackText,
                    callback = object : IPgFeedbackCallback {

                        override fun onSuccess() {
                            logger.log(Level.INFO, "Feedback successfully played.")
                        }

                        override fun onError(error: PgError) {
                            val errorMessage = "An Error occurred during triggerFeedback: $error"
                            logger.log(Level.WARNING, errorMessage)
                            runOnUiThread {
                                Toast.makeText(this@ScannerActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            )
        }
        //setting first Item as selected by default
        radioGroup.check(feedbackId1RB.id)

        //set image configurations
        setDefaultImageConfigurations()
        takeImageButton.setOnClickListener {
            takeImage()
        }

        defaultFeedbackSwitch.setOnClickListener {
            val config = PgScannerConfig(isDefaultScanAckEnabled = defaultFeedbackSwitch.isChecked)

            defaultFeedbackSwitch.isEnabled = false

            pgManager.setScannerConfig(config, object : IPgScannerConfigCallback {

                override fun onScannerConfigSuccess(config: PgScannerConfig) {
                    runOnUiThread {
                        logger.log(Level.INFO, "Successfully updated config on scanner")
                        defaultFeedbackSwitch.isEnabled = true
                    }
                }

                override fun onError(error: PgError) {
                    runOnUiThread {
                        val errorMessage = "Could not set config on scanner: $error"
                        logger.log(Level.WARNING, errorMessage)
                        Toast.makeText(this@ScannerActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        // restore old state
                        defaultFeedbackSwitch.toggle()
                        defaultFeedbackSwitch.isEnabled = true
                    }
                }
            })
        }
    }

    private fun setDefaultImageConfigurations() {
        val imageConfig = PgImageConfig()
        jpegQualityEditText.setText(imageConfig.jpegQuality.toString())
        val defaultTimeout = DEFAULT_IMAGE_TIMEOUT
        timeoutEditText.setText(defaultTimeout.toString())
    }

    private fun takeImage() {
        var timeout = DEFAULT_IMAGE_TIMEOUT
        var quality = 20

        try {
            timeout = timeoutEditText.text.toString().toInt()
            quality = jpegQualityEditText.text.toString().toInt()
        } catch (e: NumberFormatException) {
            logger.log(Level.WARNING, "use positive numbers only")
        }

        val resolution = when (resolutionRadioGroup.checkedRadioButtonId) {
            R.id.highResolution -> ImageResolution.RESOLUTION_1280_800
            R.id.mediumResolution -> ImageResolution.RESOLUTION_640_400
            R.id.lowResolution -> ImageResolution.RESOLUTION_320_200
            else -> ImageResolution.RESOLUTION_640_400
        }

        val config = PgImageConfig(quality, resolution)
        val imageCallback = object : IPgImageCallback {
            override fun onImageReceived(image: PgImage) {
                val bmp = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size)
                runOnUiThread {
                    imageTaken.setImageBitmap(bmp)
                }
            }

            override fun onError(error: PgError) {
                runOnUiThread {
                    Toast.makeText(this@ScannerActivity, "error code is $error", Toast.LENGTH_LONG).show()
                }
            }
        }
        pgManager.takeImage(config, timeout, imageCallback)
    }


    private fun getFeedbackId(): PgPredefinedFeedback {
        return when (radioGroup.checkedRadioButtonId) {
            feedbackId1RB.id -> PgPredefinedFeedback.SUCCESS
            feedbackId2RB.id -> PgPredefinedFeedback.ERROR
            feedbackId3RB.id -> PgPredefinedFeedback.SPECIAL_1
            else -> PgPredefinedFeedback.ERROR
        }
    }

    override fun onResume() {
        super.onResume()

        pgManager.ensureConnectionToService(this.applicationContext)
        updateButtonStates()
    }

    private fun updateButtonStates() {
        runOnUiThread {
            updateServiceConnectionButtonState()
            updateScannerConnectionButtonState()
        }
    }

    private fun updateScannerConnectionButtonState() {
        if (serviceConnectionState != ServiceConnection.CONNECTED) {
            connectScannerPinnedBtn.setText(R.string.pair_scanner)
            connectScannerRegularBtn.setText(R.string.pair_scanner)
        } else if (scannerConnectionState == ScannerConnection.CONNECTED) {
            connectScannerPinnedBtn.setText(R.string.scanner_connected)
            connectScannerRegularBtn.setText(R.string.scanner_connected)
        } else {
            connectScannerPinnedBtn.setText(R.string.pair_scanner)
            connectScannerRegularBtn.setText(R.string.pair_scanner)
        }
    }

    private fun updateServiceConnectionButtonState() {
        when (serviceConnectionState) {
            ServiceConnection.CONNECTING -> {
                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connecting)
            }
            ServiceConnection.CONNECTED -> {
                logger.log(Level.INFO, "Connection to ProGlove SDK Service successful.")

                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connected)
            }
            ServiceConnection.DISCONNECTED -> {
                serviceConnectBtn.isEnabled = true
                serviceConnectBtn.setText(R.string.connect_service)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        pgManager.unsubscribeFromScans(this)
        pgManager.unsubscribeFromServiceEvents(this)
    }

    /*
     * IServiceOutput Implementation BEGIN
     */

    override fun onServiceConnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnection.CONNECTED
            logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
            updateButtonStates()
        }
    }

    override fun onServiceDisconnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnection.DISCONNECTED
            logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
            updateButtonStates()
        }
    }

    /*
     * IServiceOutput Implementation END
     */

    /*
     * IScannerOutput Implementation:
     */

    override fun onBarcodeScanned(barcodeScanResults: BarcodeScanResults) {
        runOnUiThread {
            inputField.text = barcodeScanResults.barcodeContent
            barcodeScanResults.symbology?.let { symbology ->
                symbologyResult.text = symbology
                if (symbology.isNotEmpty()) {
                    Toast.makeText(this, "Got barcode: ${barcodeScanResults.barcodeContent} with symbology ${barcodeScanResults.symbology}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Got barcode: ${barcodeScanResults.barcodeContent} with no symbology", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onScannerConnected() {
        runOnUiThread {
            scannerConnectionState = ScannerConnection.CONNECTED
            updateButtonStates()
        }
    }

    override fun onScannerDisconnected() {
        runOnUiThread {
            scannerConnectionState = ScannerConnection.DISCONNECTED

            // Connecting a new scanner will reset this config to default, which is true
            defaultFeedbackSwitch.isChecked = true
            updateButtonStates()
        }
    }

    override fun onScannerStateChanged(status: ConnectionStatus) {
        runOnUiThread {
            Toast.makeText(this, "Scanner State: $status", Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * End of IScannerOutput Implementation
     */

    companion object {
        
        const val DEFAULT_IMAGE_TIMEOUT = 10000
    }
}

enum class ScannerConnection {
    CONNECTING,
    CONNECTED,
    DISCONNECTED
}

enum class ServiceConnection {
    CONNECTING,
    CONNECTED,
    DISCONNECTED
}

enum class DisplayConnection {
    CONNECTING,
    CONNECTED,
    DISCONNECTED
}
