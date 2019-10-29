# ProGlove Connect Installation Guide


## Installation

1. To install the app on your device, please download the file “ProGlove Connect 0.15.0”
    * directly to your  Android device
    * first to a PC and then transfer it to your device
2. Go to Files on the Android device and click on the ProGlove-Connect.apk file.
3. The installation process should start immediately. After the installation process is over, two different app icons should appear regarding the ProGlove Connect Beta app. 
4. One is the PG Config icon which allows you to choose your integration path and configure your scanning process (is explained down below).
5. The other one is the PG Work icon, which can be used to easily connect to your previously configured MARK scanner. 
6. After an integration path is chosen, you should be directed to the main Homepage. 
7. The first time the app is used, there will be two errors on the top of this page regarding keyboard settings. Please click fix and allow ProGlove keyboard access to enable the ProGlove Connect Beta app to work properly on your Android device.

## Integration

There are 3 different main use cases for our ProGlove Connect Beta app and the integration process for them are also different. Please read through the guide and choose the process that best fits to your company’s needs.

### Software Keyboard

This path is a very easy Plug&Play integration.  It is especially recommended for customers which just want to use the super easy Scan2Pair functionality. It will input the scanned barcode directly to the text field of the currently running application. 
1. The only thing you need to do after the app is installed to your widget, is to pair your MARK scanner and device.

### Intent

This path is recommended for applications that already use android intent commands to exchange data. It can be used, if a ProGlove MARK is a drop-in replacement for an existing configuration using the Android Intent API. For example, the Velocity and Stay-Linked Browsers have already implemented a very easy intent interface to the MARK scanners. But it can also be used for existing or future customer apps, that use intents to communicate with barcode scanners.
1. After the ProGlove app is installed to your widget, the app your company uses with the incoming data from the scanners need to be adjusted using the “Intent Integration Guide 0.14.0” that can be found in the cloudsmith repository. 
2. After the adjustments have been made, all that is left is pairing the Mark scanner and your device.


### SDK Integration

This path allows and requires code-level integration into your application which allows full control over connection and callback features.
1. You can find detailed information about the implementation process and coding examples in the “SDK Integration Guide 0.9.0” in the cloudsmith repository.

## Configuration

There is the possibility to automatically configure the ProGlove Connect Beta app for large scale deployments. Please look at the “Batch Configuration” file in the cloudsmith repository for further details.