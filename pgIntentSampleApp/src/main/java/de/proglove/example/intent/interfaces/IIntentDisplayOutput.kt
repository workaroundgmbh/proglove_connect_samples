package de.proglove.example.intent.interfaces

import de.proglove.example.intent.enums.DeviceConnectionStatus
import de.proglove.example.intent.enums.DisplayDeviceType

/**
 * Display device callback.
 */
interface IIntentDisplayOutput {

    /**
     * Callback method for a button being pressed on the display device.
     *
     * @param buttonId Pressed button ID.
     * @param screenContext The screen context from which the button was pressed.
     */
    fun onButtonPressed(buttonId: String, screenContext: String)

    /**
     * A more detailed callback for the display status.
     *
     * To understand the current state (searching for devices, timeouts, etc) subscribe to this method.
     *
     * @param status The reported status of the device.
     */
    fun onDisplayStateChanged(status: DeviceConnectionStatus)

    /**
     * Callback method for the display device type change.
     *
     * @param displayType The new display device type.
     */
    fun onDisplayDeviceTypeChanged(displayType: DisplayDeviceType)

    /**
     * Callback method for the display event received.
     *
     * @param event The event received from the display device.
     * @param context The screen context received from the display device.
     */
    fun onDisplayEventReceived(event: String, context: String)
}
