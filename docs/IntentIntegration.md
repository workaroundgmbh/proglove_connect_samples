# ProGlove Intent API

The ProGlove Intent API enables an easy integration of  MARK 2 Scanners with other Apps.

Apps that are already enabled to work with the Zebra DataWedge should work with minimal configuration changes.

## Basics

The ProGlove Connect App sends out certain things via Broadcast Intent, and listens for certain Broadcast Intents in response.
These can originate from any App on this Device, including Cordova/React applications.

**Important: MARK has to be paired using the ProGlove Connect App before this API will work!**

### Logging/Output

Unless otherwise specified, the API Calls have no return value. In these cases, the results will be logged to the Android console and can be viewed by filtering logcat to de.proglove.connect.intent.api or PGAPI

     adb logcat -e PGAPI

## API

### Sent out from the ProGlove Connect App

### Receiving Barcode Data

**Only enabled when the Rule is configured**

In the ProGlove Connect App, configure a rule to pass Incoming Barcode Events to an Intent ('Paste Buffer as Intent').
In this rule, the action name of the outgoing intent can be configured.

 * Action: can be configured, default: _com.proglove.api.BARCODE_
 * Extras:
   * String in _com.proglove.api.extra.BARCODE_DATA_ contains the Barcode Data
   * for easier integration, we also supply the barcode data in the extra _com.symbol.datawedge.data_string_
   * String in _com.proglove.api.extra.BARCODE_SYMBOLOGY_ contains the Barcode's Symbology, e.g. "EAN-13" (See [Symbologies](Symbologies.md) for the full list of supported Symbologies)
   * for easier integration, we also supply the symbology data in the extra _com.symbol.datawedge.label_type_

In order to receive broadcast intents the following steps are needed (code is in Kotlin):

1) Implement a broadcast receiver (in this case the class is called MessageHandler):
```kotlin
    class MessageHandler : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "com.proglove.api.BARCODE") {
                intent.getStringExtra("com.proglove.api.extra.BARCODE_DATA")
                intent.getStringExtra("com.proglove.api.extra.BARCODE_SYMBOLOGY")
            }
        }
    }
```

2) define an IntentFilter filtering for the specified actions:
```kotlin
    val messageHandler: MessageHandler = MessageHandler()
    val filter = IntentFilter()
    filter.addAction("com.proglove.api.BARCODE")
    filter.addCategory(Intent.CATEGORY_DEFAULT)
```

3) Somewhere where a context is available (usually an Activity's or Service's onCreate):
```kotlin
    context.registerReceiver(messageHandler, filter)
```
    Do not forget to unregister the receiver again, for example in onDestroy:
```kotlin
    context.unregisterReceiver(messageHandler)
```

## Getting Connection State

Once the pairing process is started, the ProGlove Connect App will send out the current connection State of the MARK

* Action: _com.proglove.api.SCANNER_STATE_
* Extra: String in _com.proglove.api.extra.SCANNER_STATE_

Example code to get scanner state events (in Kotlin):

1) Implement a broadcast receiver (in this case the class is called MessageHandler):
```kotlin
    class MessageHandler : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == "com.proglove.api.SCANNER_STATE") {
                intent.getStringExtra("com.proglove.api.extra.SCANNER_STATE")
            }
        }
    }
```

2) Define an IntentFilter for the desired actions:
```kotlin
    val messageHandler: MessageHandler = MessageHandler()
    val filter = IntentFilter()
    filter.addAction("com.proglove.api.SCANNER_STATE")
    filter.addCategory(Intent.CATEGORY_DEFAULT)
```

3) Finally register the broadcast receiver instance with a context. The usual place for this call would be in the onCreate method of a Service or an Activity class:
```kotlin
    context.registerReceiver(messageHandler, filter)
```
   Do not forget to unregister the receiver again, for example in onDestroy:
```kotlin
    context.unregisterReceiver(messageHandler)
```

The passed Status String can be one of:

 * "CONNECTED": MARK 2 is connected, the user can start scanning
 * "DISCONNECTED": No MARK 2 is connected
 * "CONNECTING": In the process of establishing a MARK 2 BLE Connection
 * "ERROR": Something went wrong trying to establish the MARK2 Connection or the BLE Search in general. Consult the ProGlove Connect App.
 * "RECONNECTING": Lost connection to a previously connected MARK2, trying to re-establish the connection
 * "SEARCHING": Searching for a MARK 2 and showing the PAIRING Screen (including the QR code)

The two above examples can be unified to one class handling all events, by adding the other Action the the IntentFilter object. Then onReceive gets called for both kinds of events.

## Input to the ProGlove Connect App

### Disconnecting MARK

To disconnect a connected MARK, send a Broadcast Intent to:
 * Action: _com.proglove.api.DISCONNECT_
 * No Extras

Example code:

    val intent = Intent()
    intent.setAction("com.proglove.api.DISCONNECT")
    sendBroadcast(intent)

See [Symbologies](Symbologies.md) for the list of supported symbologies.