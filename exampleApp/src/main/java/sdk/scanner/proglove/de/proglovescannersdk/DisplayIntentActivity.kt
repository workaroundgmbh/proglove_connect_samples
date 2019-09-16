package sdk.scanner.proglove.de.proglovescannersdk

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.proglove.sdk.ConnectionStatus
import de.proglove.sdk.utils.Logger
import kotlinx.android.synthetic.main.activity_display_intent.connectD3Btn
import kotlinx.android.synthetic.main.activity_display_intent.d3Name
import kotlinx.android.synthetic.main.activity_display_intent.disconnectD3Btn
import kotlinx.android.synthetic.main.activity_display_intent.displayStateOutput
import kotlinx.android.synthetic.main.activity_display_intent.getDisplayState
import kotlinx.android.synthetic.main.activity_display_intent.sendTestScreenD3Btn
import kotlinx.android.synthetic.main.activity_display_intent.sendTestScreenD3Btn2
import kotlinx.android.synthetic.main.activity_display_intent.sendTestScreenD3BtnFailing

class DisplayIntentActivity : AppCompatActivity(), IIntentDisplayOutput {

    var displayConnectionState = DisplayConnection.DISCONNECTED
    val messageHandler: MessageHandler = MessageHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_intent)

        registerReceiver(messageHandler, messageHandler.filter)

        messageHandler.registerDisplayOutput(this)

        connectD3Btn.setOnClickListener {
            val deviceNameFromInput = d3Name.text.toString()
            messageHandler.sendConnectD3(
                    if (deviceNameFromInput.isNotEmpty()) deviceNameFromInput else "D3 hello world"
            )
        }

        disconnectD3Btn.setOnClickListener {
            messageHandler.sendDisconnectD3()
        }

        getDisplayState.setOnClickListener {
            messageHandler.requestDisplayState()
        }

        sendTestScreenD3Btn.setOnClickListener {
            messageHandler.sendTestScreen("PG1", "1|Bezeichnung|Kopfairbag|2|Fahrzeug-Typ|Hatchback|3|Teilenummer|K867 86 027 H3", "|")
        }

        sendTestScreenD3Btn2.setOnClickListener {
            messageHandler.sendTestScreen("PG2", "1|Stück|1 Pk|2|Bezeichnung|Gemüsemischung|3|Stück|420|4|Bezeichnung|Früchte Müsli|5|Stück|30|6|Bezeichnung|Gebäck-Stangen", "|")
        }

        sendTestScreenD3BtnFailing.setOnClickListener {
            messageHandler.sendTestScreen("PG2", "1|Header1|Content1|8|Header2|Content2", "|")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(messageHandler)
        messageHandler.unregisterDisplayOutput(this)
    }

    private fun updateConnectionLabel() {
        runOnUiThread {
            if (displayConnectionState == DisplayConnection.CONNECTED) {
                displayStateOutput.setText(R.string.display_connected)
            } else if (displayConnectionState == DisplayConnection.DISCONNECTED) {
                displayStateOutput.setText(R.string.display_disconnected)
            }
        }
    }

    override fun onButtonPressed(buttonId: String) {
        Toast.makeText(this, "Button $buttonId pressed", Toast.LENGTH_SHORT).show()
    }

    override fun onDisplayStateChanged(status: ConnectionStatus) {
        Logger.d("Did receive display status: $status")
        displayConnectionState = when (status) {
            ConnectionStatus.CONNECTED -> {
                DisplayConnection.CONNECTED
            }
            ConnectionStatus.DISCONNECTED -> {
                DisplayConnection.DISCONNECTED
            }
            else -> {
                DisplayConnection.CONNECTING
            }
        }
        updateConnectionLabel()
    }
}

interface IIntentDisplayOutput {

    fun onButtonPressed(buttonId: String)

    fun onDisplayStateChanged(status: ConnectionStatus)
}