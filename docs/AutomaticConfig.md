# Batch Configuration

To automatically configure the ProGlove Connect App for large scale deployment, follow these steps.
We assume you have an MDM or something similar set up that can set permissions and push files to your target devices.

### Preparation - get the configuration

Setup the app on a test device manually and go through the configuration steps.
Use either the wizard or, if necessary, more complicated custom rules.
After you have tested your configuration, tap on the "..." on the top menu and select "Export Configuration".
A popup dialog will come up asking for a place to store the configuration.
Store the file in a location known to you and copy it to a computer for distribution

### Setup - the script

On a new device, after app installation, you need to do two things:
 - import the configuration
 - grant the necessary permissions

Depending on your setup, there are multiple ways to do this.

#### Importing Config
To import the config, push the config file to **/sdcard/Android/data/de.proglove.connect/files/ProGloveConfig.pgconf** (this does NOT require setting the storage permission beforehand)
and start the application once to import the configuration.

Once the file is imported, it will be removed from it's original location and moved to '/sdcard/Android/data/de.proglove.connect/files/handledConfig/ProGloveConfig.pgconf'.

Afterwards if you want to make configuration changes, you can always put a new file to **/sdcard/Android/data/de.proglove.connect/files/ProGloveConfig.pgconf**. On the next start of the application, it will be read automatically.

#### Permissions
To grant the necessary permissions, execute the following shell commands on android:
````bash
pm grant de.proglove.connect android.permission.ACCESS_FINE_LOCATION
````

If you want to try this out over adb, you can execute
````bash
adb shell <<EOF
pm grant de.proglove.connect android.permission.ACCESS_FINE_LOCATION
EOF
````
