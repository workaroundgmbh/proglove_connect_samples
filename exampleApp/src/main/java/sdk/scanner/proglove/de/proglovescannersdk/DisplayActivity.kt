package sdk.scanner.proglove.de.proglovescannersdk

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.proglove.sdk.ConnectionStatus
import de.proglove.sdk.IServiceOutput
import de.proglove.sdk.PgError
import de.proglove.sdk.PgManager
import de.proglove.sdk.button.ButtonPress
import de.proglove.sdk.button.IButtonOutput
import de.proglove.sdk.display.IDisplayOutput
import de.proglove.sdk.display.IPgSetScreenCallback
import de.proglove.sdk.display.PgScreenData
import de.proglove.sdk.display.PgTemplateField
import kotlinx.android.synthetic.main.activity_display.connectD3Btn
import kotlinx.android.synthetic.main.activity_display.d3Name
import kotlinx.android.synthetic.main.activity_display.disconnectD3Btn
import kotlinx.android.synthetic.main.activity_display.displayStateOutput
import kotlinx.android.synthetic.main.activity_display.sendTestScreenD3Btn
import kotlinx.android.synthetic.main.activity_display.sendTestScreenD3Btn2
import kotlinx.android.synthetic.main.activity_display.sendTestScreenD3BtnFailing
import kotlinx.android.synthetic.main.activity_display.serviceConnectBtn
import java.util.logging.Level
import java.util.logging.Logger

class DisplayActivity : AppCompatActivity(), IDisplayOutput, IServiceOutput, IButtonOutput {

    private val logger = Logger.getLogger("sample-logger")
    private val pgManager = PgManager(logger)

    private var serviceConnectionState = ServiceConnection.DISCONNECTED
    private var displayConnectionState = ScannerConnection.DISCONNECTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        pgManager.subscribeToServiceEvents(this)
        pgManager.subscribeToDisplayEvents(this)
        pgManager.subscribeToButtonPresses(this)

        serviceConnectBtn.setOnClickListener {
            pgManager.ensureConnectionToService(this.applicationContext)
            updateButtonStates()
        }

        connectD3Btn.setOnClickListener {
            pgManager.connectDisplay(d3Name.text.toString())
        }

        disconnectD3Btn.setOnClickListener {
            pgManager.disconnectDisplay()
        }

        sendTestScreenD3Btn.setOnClickListener {
            val data = PgScreenData("PG1", arrayOf(
                    PgTemplateField(1, "Bezeichnung", "Kopfairbag"),
                    PgTemplateField(2, "Fahrzeug-Typ", "Hatchback"),
                    PgTemplateField(3, "Teilenummer", "K867 86 027 H3")
            ))
            val callback = object : IPgSetScreenCallback {
                override fun onError(error: PgError) {
                    runOnUiThread {
                        Toast.makeText(this@DisplayActivity, "Got error setting text: $error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onSuccess() {
                    runOnUiThread {
                        Toast.makeText(this@DisplayActivity, "set screen successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            pgManager.setScreen(data, callback)
        }

        sendTestScreenD3Btn2.setOnClickListener {
            val data = PgScreenData("PG2", arrayOf(
                    PgTemplateField(1, "Stueck", "1 Pk"),
                    PgTemplateField(2, "Bezeichnung", "Gemuesemischung"),
                    PgTemplateField(3, "Stueck", "420"),
                    PgTemplateField(4, "Bezeichnung", "Fruechte Muesli"),
                    PgTemplateField(5, "Stueck", "30"),
                    PgTemplateField(6, "Bezeichnung", "Gebaeck-Stangen")
            ))
            val callback = object : IPgSetScreenCallback {
                override fun onError(error: PgError) {
                    runOnUiThread {
                        Toast.makeText(this@DisplayActivity, "Got error setting text: $error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onSuccess() {
                    runOnUiThread {
                        Toast.makeText(this@DisplayActivity, "set screen successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            pgManager.setScreen(data, callback)
        }

        sendTestScreenD3BtnFailing.setOnClickListener {
            val data = PgScreenData("PG1", arrayOf(
                    PgTemplateField(1, "now this is the story", "all about how"),
                    PgTemplateField(2, "my life got flipped", "turned upside down"),
                    PgTemplateField(3, "and I'd like to take", "a minute just sit right there"),
                    PgTemplateField(4, "I'll tell you how I become", "the prince of a town called Bel Air")

            ))
            val callback = object : IPgSetScreenCallback {
                override fun onError(error: PgError) {
                    runOnUiThread {
                        Toast.makeText(this@DisplayActivity, "Got error setting text: $error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onSuccess() {
                    runOnUiThread {
                        Toast.makeText(this@DisplayActivity, "set screen successfully", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            pgManager.setScreen(data, callback)
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
            updateDisplayConnectionUiState()
        }
    }

    private fun updateDisplayConnectionUiState() {
        if (serviceConnectionState != ServiceConnection.CONNECTED) {
            displayStateOutput.setText(R.string.display_disconnected)
        } else if (displayConnectionState == ScannerConnection.CONNECTED) {
            displayStateOutput.setText(R.string.display_connected)
        } else {
            displayStateOutput.setText(R.string.display_disconnected)
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

        pgManager.unsubscribeFromDisplayEvents(this)
        pgManager.unsubscribeFromServiceEvents(this)
        pgManager.unsubscribeFromButtonPresses(this)
    }

    /*
     * IServiceOutput Implementation BEGIN
     */

    override fun onServiceConnected() {
        serviceConnectionState = ServiceConnection.CONNECTED
        logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
        updateButtonStates()
    }

    override fun onServiceDisconnected() {
        serviceConnectionState = ServiceConnection.DISCONNECTED
        logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
        updateButtonStates()
    }

    /*
     * IServiceOutput Implementation END
     */

    /*
     * IDisplayOutput Implementation:
     */

    override fun onDisplayConnected() {
        Log.i("DISPLAY", "connected")
        displayConnectionState = ScannerConnection.CONNECTED
        updateButtonStates()
    }

    override fun onDisplayDisconnected() {
        Log.i("DISPLAY", "disconnected")
        displayConnectionState = ScannerConnection.DISCONNECTED
        updateButtonStates()
    }

    override fun onDisplayStateChanged(status: ConnectionStatus) {
        Log.i("DISPLAY", "newState: $status")
        runOnUiThread {
            Toast.makeText(this, "Display State: $status", Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * End of IDisplayOutput Implementation
     */

    /*
     * IButtonOutput Implementation:
     */
    override fun onButtonPressed(buttonPressed: ButtonPress) {
        runOnUiThread {
            Toast.makeText(this, "Button Pressed: ${buttonPressed.id}", Toast.LENGTH_SHORT).show()
        }
    }
    /*
     * End of IButtonOutput Implementation
     */
}
