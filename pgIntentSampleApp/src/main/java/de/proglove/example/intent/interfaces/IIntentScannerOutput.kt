package de.proglove.example.intent.interfaces

import de.proglove.example.intent.enums.DeviceConnectionStatus

/**
 * Scanner device callback.
 */
interface IIntentScannerOutput {

    /**
     * The default scan feedback enabled setter/getter.
     */
    var defaultFeedbackEnabled: Boolean

    /**
     * A callback method, that is called only once for each scanned barcode.
     *
     * @param barcode The scanned barcode as a string representation, based on the currently configured scanner
     * settings.
     * @param symbology (Optional) Symbology of the scanned barcode.
     * @param screenContext The screen context from which the barcode was scanned.
     */
    fun onBarcodeScanned(barcode: String, symbology: String, screenContext: String)

    /**
     * A more detailed callback for the scanner status.
     *
     * To understand the current state (searching for devices, timeouts, etc) subscribe to this method.
     *
     * @param status The reported status of the device.
     */
    fun onScannerStateChanged(status: DeviceConnectionStatus)

    /**
     * A callback method, that is called when information about configuration profiles is received.
     *
     * @param profileIds Collection of all configuration profiles' names (IDs)
     * @param activeProfileId Active profile's ID
     */
    fun onConfigProfilesReceived(profileIds: Array<String>, activeProfileId: String)


    /**
     * A callback method, that is called after DeviceVisibilityInfo were obtained.
     */
    fun onDeviceVisibilityInfoReceived(serialNumber: String,
                                       firmwareRevision: String,
                                       batteryLevel: Int,
                                       bceRevision: String,
                                       modelNumber: String,
                                       manufacturer: String,
                                       deviceBluetoothMacAddress: String,
                                       appVersion: String)
}