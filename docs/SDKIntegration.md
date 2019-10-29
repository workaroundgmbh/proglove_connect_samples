# ProGlove MARK Scanner SDK

## Introduction

The ProGlove MARK Scanner SDK is a fast and easy way to integrate ProGlove MARK2
into your application. The SDK works by connecting to the ProGlove Connect App,
which in turn connects (via BLE) to the MARK2 Scanner.

This setup allows us to take the pain of handling, establishing and holding the BLE connection to MARK2
in a central, easy to update place (the ProGlove Connect App) - while your application can focus on the actual (business) process.

## Setup
Please be aware that this is a trial version of the App/SDK.
Future productive versions require a one time internet connection in order to register and unlock your App/SDK.

## Installation Steps

### 1. Prerequisites
You need to have the ProGlove Connect App on your test device - it can be downloaded either via Google Play or via a provided APK.

Further, the ProGlove Scanner SDK requires a minimum Android Api Level of 18. Please make sure your app uses at least this level.

Finally, after installing the ProGlove Connect App, open the app up and select the "Scan2Pair" function.
You will be asked for File Storage and Location Permission. Please grant both Permissions - otherwise, the Pairing from the SDK might not work correctly.
If you can see the Pairing QR-Code in the App, all is well and you can continue with the SDK integration (Step 2).


### 2. Add library

To download the AAR, add this to your gradle repository (top level)

```groovy
allprojects {
  repositories {
      maven {
          url "https://dl.cloudsmith.io/basic/proglove/pgconnect-prod/maven/"
          credentials {
              proglove_user "$repositoryUser"
              proglove_password "$repositoryPassword"
          }
          authentication {
             basic(BasicAuthentication)
          }
      }
   }
}
```

and then add this to your apps build.gradle in the dependencies block :

```groovy
implementation 'de.proglove:scannerapi:0.9.0'
```

Finally, use the username and password provided by ProGlove in your `gradle.properties`:
```
proglove_user=USERNAME
proglove_password=PASSWORD
```

Your project should now build/assemble without problems.

### 3. Integration

To integrate the Mark Scanner, you need somewhere to hold a reference to it.
If you need access to the scanner in multiple places in your application, the SDK can be instantiated in the Application Android Object. However, any other Context object will work as well (Activity or Service).
In the `onCreate`, instantiate the Scanner Manager and connect it to the ProGlove Connect App

Please note: The example below is taken from the example app, where everything is implemented in a single activity. You can (and should) obviously adapt this to your needs.

```kotlin
val scannerManager: ScannerManager = ScannerManager()

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate()
    val result = scannerManager.connect(this.applicationContext)
}
```

Check the return value (a boolean) to see if the service binding worked.
If the correct version of ProGlove Connect App is installed on your device, you will get a positive response.

From this point on, access the scannerManager variable to use the SDK.

### (Optional) Callback for service connection
The connection to the SDK Service is very fast and you will most likely not encounter problems by proceeding with the pairing process right away.
But it is still asynchronous. So you might want to register for callbacks of the Service connection.
Before calling `scannerManager.connect(...)` register your implementation of the `IServiceOutput` interface (In this case `this`).
Also make sure to unsubscribe from these events. `onCreate` and `onDestroy` are great for that.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {

    //...
    scannerManager.subscribeToServiceEvents(this)
}
   /*
    */
override fun onDestroy() {
    super.onDestroy()

    scannerManager.unsubscribeFromServiceEvents(this)
}
```

### Callback for scanned barcodes
Mainly, you will want to be informed about new barcode scans. To get this,
call the `scannerManager.subscribeToScans()` function and implement the `IScannerOutput` interface.
For example, in the same  `onCreate` of our sample app we subscribe to the scanner info.
And we unsubscribe in the `onDestroy` function.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {

     //...

     scannerManager.subscribeToScans(this)

     //...
}

override fun onDestroy() {
     super.onDestroy()
     scannerManager.unsubscribeFromScans(this)
}
```

To do this, our Activity needs to implement the `IScannerOutput` interface:

```kotlin
//
// -- IScannerInput --
//

override fun onBarcodeScanned(barcodeScanResults: BarcodeScanResults) {
      // the scanner input will come on background threads, make sure to execute this on the UI Thread
    runOnUiThread {
        if(barcodeScanResults.symbology.isNotEmpty()) {
            Toast.makeText(this, "Got barcode: ${barcodeScanResults.barcodeContent} with symbology: ${barcodeScanResults.symbology}", Toast.LENGTH_LONG).show()
            // do some custom logic here to react on received barcodes and symbology
        } else {
            Toast.makeText(this, "Got barcode: ${barcodeScanResults.barcodeContent}", Toast.LENGTH_LONG).show()
            // not every Mark currently has the ability to send the barcode symbology
        }
    }
}

override fun onScannerConnected() {
        // let the user know that the scanner is connected
}

override fun onScannerDisconnected() {
     // Inform the user that the scanner has been disconnected
}
```

See [Symbologies](Symbologies.md) for the list of supported symbologies.

### 4. Mark Scanner State

The `IScannerOutput` callback  and `ScannerManager` provide you with several methods for tracking the MARK state.

As you can see above, the IScannerInput Interface will be called whenever a MARK Scanner connects or disconnects.

You can also query the `ScannerManager` methods.

To disconnect, call the `ScannerManager.disconnect()` method.

### 5. Pair with Mark2

To start the pairing process call `startPairing()` on your `ScannerManager` reference. The ProGlove Connect App will take over and show the paring QR-Code.
After your device was paired with a Mark2 or the process was canceled by the user, your activity will be brought back to the foreground.
Your registered `IScannerOutput` implementations will be called according to the updated scanner state.

Important Note for Android's pinned application mode:
If your App runs inside Android's pinned Application mode you have to use `startPairingFromPinnedActivity(activity)` instead.
`activity` must be an Activity running in the currently pinned Task. Pairing will work the same as with `startPairing()` but will be working in pinning Mode.
`scannerManager.connect(this.applicationContext)` should still be called with the ApplicationContext. That way a MARK will stay connected regardless of Lifecycle of a single Activity.

Startint the pairing from Background Task:
Only `startPairing()` is able to work from a Service's Context.

### Logging

To facilitate with debugging, the ScannerManager can be injected with a `java.util.Logger` object that will receive all
log information.

```
    private val scannerManager = ScannerManager(logger)
```

## Examples

The Example App is a (very simple) implementation using the SDK to display the last scanned value in a text field.
You can use this application to see how to implement the MARK2 Scanner API

### Testing without a MARK2

To send test data without using an actual Mark II, execute the following from your developer machine:

    adb shell "am broadcast -a de.proglove.core.events.providers.ACTION.TEST_SDK_DATA -e de.proglove.core.events.providers.EXTRA.SCANNED_DATA \"Test Barcode Text\""

Note:
The correct use of the quotation marks (") is important. An incorrect broadcast will not throw an error but will fail silently.

#### Emulating scanner (dis)connect without a Mark II
For connect simulation:

    adb shell am broadcast -a de.proglove.core.events.providers.ACTION.TEST_SDK_CONNECT

For disconnect simulation:

    adb shell am broadcast -a de.proglove.core.events.providers.ACTION.TEST_SDK_DISCONNECT


## Updating/Versioning

This is currently a test version. We do not expect this version to work longer than 30 days. It will not work in production and will not be supported for longer than that
