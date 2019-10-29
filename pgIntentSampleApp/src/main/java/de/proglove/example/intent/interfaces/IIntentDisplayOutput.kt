package de.proglove.example.intent.interfaces

import de.proglove.example.intent.enums.DeviceConnectionStatus

/**
 * Display device callback.
 */
interface IIntentDisplayOutput {

    /**
     * Callback method for a button being pressed on the display device.
     *
     * @param buttonId Pressed button ID.
     */
    fun onButtonPressed(buttonId: String)

    /**
     * A more detailed callback for the display status.
     *
     * To understand the current state (searching for devices, timeouts, etc) subscribe to this method.
     *
     * @param status The reported status of the device.
     */
    fun onDisplayStateChanged(status: DeviceConnectionStatus)
}