package de.proglove.example.sdk

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.proglove.example.common.DisplaySampleData
import de.proglove.sdk.ConnectionStatus
import de.proglove.sdk.IServiceOutput
import de.proglove.sdk.PgError
import de.proglove.sdk.PgManager
import de.proglove.sdk.button.BlockPgTriggersParams
import de.proglove.sdk.button.ButtonPress
import de.proglove.sdk.button.IBlockPgTriggersCallback
import de.proglove.sdk.button.IButtonOutput
import de.proglove.sdk.button.IPgTriggersUnblockedOutput
import de.proglove.sdk.button.PredefinedPgTrigger
import de.proglove.sdk.commands.PgCommand
import de.proglove.sdk.commands.PgCommandParams
import de.proglove.sdk.configuration.IPgConfigProfileCallback
import de.proglove.sdk.configuration.IPgGetConfigProfilesCallback
import de.proglove.sdk.configuration.IPgScannerConfigurationChangeOutput
import de.proglove.sdk.configuration.PgConfigProfile
import de.proglove.sdk.configuration.PgScannerConfigurationChangeResult
import de.proglove.sdk.configuration.ScannerConfigurationChangeStatus
import de.proglove.sdk.display.IDisplayOutput
import de.proglove.sdk.display.IPgSetScreenCallback
import de.proglove.sdk.display.PgScreenData
import de.proglove.sdk.display.PgTemplateField
import de.proglove.sdk.display.RefreshType
import de.proglove.sdk.scanner.BarcodeScanResults
import de.proglove.sdk.scanner.DeviceVisibilityInfo
import de.proglove.sdk.scanner.IPgDeviceVisibilityCallback
import de.proglove.sdk.scanner.IPgFeedbackCallback
import de.proglove.sdk.scanner.IPgImageCallback
import de.proglove.sdk.scanner.IPgScannerConfigCallback
import de.proglove.sdk.scanner.IScannerOutput
import de.proglove.sdk.scanner.ImageResolution
import de.proglove.sdk.scanner.PgImage
import de.proglove.sdk.scanner.PgImageConfig
import de.proglove.sdk.scanner.PgPredefinedFeedback
import de.proglove.sdk.scanner.PgScannerConfig
import de.proglove.sdk.utils.IPgSetActivityGoalsCallback
import de.proglove.sdk.workerperformance.PgActivityGoals
import kotlinx.android.synthetic.main.activity_goals.activityGoalsAverageScansGoalEdit
import kotlinx.android.synthetic.main.activity_goals.activityGoalsScansGoalEdit
import kotlinx.android.synthetic.main.activity_goals.activityGoalsStepsGoalEdit
import kotlinx.android.synthetic.main.activity_goals.setActivityGoalsBtn
import kotlinx.android.synthetic.main.activity_main.blockAllTriggersButton
import kotlinx.android.synthetic.main.activity_main.blockTriggerButton
import kotlinx.android.synthetic.main.activity_main.connectScannerPinnedBtn
import kotlinx.android.synthetic.main.activity_main.connectScannerRegularBtn
import kotlinx.android.synthetic.main.activity_main.defaultFeedbackSwitch
import kotlinx.android.synthetic.main.activity_main.deviceVisibilityBtn
import kotlinx.android.synthetic.main.activity_main.disconnectDisplayBtn
import kotlinx.android.synthetic.main.activity_main.displayStateOutput
import kotlinx.android.synthetic.main.activity_main.inputField
import kotlinx.android.synthetic.main.activity_main.lastResponseValue
import kotlinx.android.synthetic.main.activity_main.pickDisplayOrientationDialogBtn
import kotlinx.android.synthetic.main.activity_main.sendFeedbackWithReplaceQueueSwitch
import kotlinx.android.synthetic.main.activity_main.sendNotificationTestScreenBtn
import kotlinx.android.synthetic.main.activity_main.sendPartialRefreshTestScreenBtn
import kotlinx.android.synthetic.main.activity_main.sendPg1ATestScreenBtn
import kotlinx.android.synthetic.main.activity_main.sendPg1TestScreenBtn
import kotlinx.android.synthetic.main.activity_main.sendPg3WithRightHeadersTestScreenBtn
import kotlinx.android.synthetic.main.activity_main.sendTestScreenBtn
import kotlinx.android.synthetic.main.activity_main.sendTestScreenBtnFailing
import kotlinx.android.synthetic.main.activity_main.serviceConnectBtn
import kotlinx.android.synthetic.main.activity_main.symbologyResult
import kotlinx.android.synthetic.main.activity_main.unblockTriggerButton
import kotlinx.android.synthetic.main.activity_main.versionOutput
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId1RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId2RB
import kotlinx.android.synthetic.main.feedback_selection_layout.feedbackId3RB
import kotlinx.android.synthetic.main.feedback_selection_layout.radioGroup
import kotlinx.android.synthetic.main.feedback_selection_layout.triggerFeedbackButton
import kotlinx.android.synthetic.main.profiles_layout.changeProfileLabel
import kotlinx.android.synthetic.main.profiles_layout.profilesRecycler
import kotlinx.android.synthetic.main.profiles_layout.refreshConfigProfilesButton
import kotlinx.android.synthetic.main.take_image_layout.imageTaken
import kotlinx.android.synthetic.main.take_image_layout.jpegQualityEditText
import kotlinx.android.synthetic.main.take_image_layout.resolutionRadioGroup
import kotlinx.android.synthetic.main.take_image_layout.takeImageButton
import kotlinx.android.synthetic.main.take_image_layout.timeoutEditText
import java.util.logging.Level
import java.util.logging.Logger

/**
 * PG SDK example for a scanner.
 */
class SdkActivity : AppCompatActivity(), IScannerOutput, IServiceOutput, IDisplayOutput, IButtonOutput, IPgTriggersUnblockedOutput, IPgScannerConfigurationChangeOutput {

    private val logger = Logger.getLogger("sample-logger")
    private val pgManager = PgManager(logger)

    private var serviceConnectionState = ServiceConnectionStatus.DISCONNECTED
    private var scannerConnected = false
    private var displayConnected = false

    private lateinit var profilesAdapter: ProfilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pgManager.subscribeToServiceEvents(this)
        pgManager.subscribeToScans(this)
        pgManager.subscribeToDisplayEvents(this)
        pgManager.subscribeToButtonPresses(this)
        pgManager.subscribeToPgTriggersUnblocked(this)
        pgManager.subscribeToPgScannerConfigurationChanges(this)

        versionOutput.text = BuildConfig.VERSION_CODE.toString()

        serviceConnectBtn.setOnClickListener {
            pgManager.ensureConnectionToService(this.applicationContext)
            updateButtonStates()
        }

        connectScannerRegularBtn.setOnClickListener {
            if (scannerConnected) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairing()
            }
        }

        connectScannerPinnedBtn.setOnClickListener {
            if (scannerConnected) {
                pgManager.disconnectScanner()
            } else {
                pgManager.startPairingFromPinnedActivity(this)
            }
        }

        triggerFeedbackButton.setOnClickListener {
            val selectedFeedbackId = getFeedbackId()

            // Creating new PgCommandParams setting the queueing behaviour
            val pgCommandParams = PgCommandParams(sendFeedbackWithReplaceQueueSwitch.isChecked)

            // Wrapping the feedback data in a PgCommand with the PgCommandData
            val triggerFeedbackCommand = selectedFeedbackId.toCommand(pgCommandParams)

            pgManager.triggerFeedback(
                    command = triggerFeedbackCommand,
                    callback = object : IPgFeedbackCallback {

                        override fun onSuccess() {
                            logger.log(Level.INFO, "Feedback successfully played.")
                            runOnUiThread {
                                lastResponseValue.text = getString(R.string.feedback_success)
                            }
                        }

                        override fun onError(error: PgError) {
                            val errorMessage = "An Error occurred during triggerFeedback: $error"
                            logger.log(Level.WARNING, errorMessage)
                            runOnUiThread {
                                Toast.makeText(this@SdkActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                lastResponseValue.text = error.toString()
                            }
                        }
                    }
            )
        }
        // setting first Item as selected by default
        radioGroup.check(feedbackId1RB.id)

        // set image configurations
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
                        lastResponseValue.text = getString(R.string.scanner_config_success)
                    }
                }

                override fun onError(error: PgError) {
                    runOnUiThread {
                        val errorMessage = "Could not set config on scanner: $error"
                        logger.log(Level.WARNING, errorMessage)
                        Toast.makeText(this@SdkActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        // restore old state
                        defaultFeedbackSwitch.toggle()
                        defaultFeedbackSwitch.isEnabled = true
                        lastResponseValue.text = error.toString()
                    }
                }
            })
        }

        refreshConfigProfilesButton.setOnClickListener {
            getConfigProfiles()
        }

        setupProfilesRecycler()

        blockTriggerButton.setOnClickListener {
            blockTrigger()
        }

        blockAllTriggersButton.setOnClickListener {
            blockAllTriggersFor10s()
        }

        unblockTriggerButton.setOnClickListener {
            unblockTrigger()
        }

        addDisplayClickListeners()

        pickDisplayOrientationDialogBtn.setOnClickListener {
            val error = pgManager.showPickDisplayOrientationDialog(this)
            if (error != null) {
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
                lastResponseValue.text = error.toString()
            }
        }

        deviceVisibilityBtn.setOnClickListener {
            obtainDeviceVisibilityInfo()
        }

        setActivityGoalsBtn.setOnClickListener {
            setActivityGoals()
        }
    }

    private fun addDisplayClickListeners() {

        val loggingCallback = object : IPgSetScreenCallback {

            override fun onError(error: PgError) {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "Got error setting text: $error", Toast.LENGTH_SHORT)
                            .show()
                    lastResponseValue.text = error.toString()
                }
            }

            override fun onSuccess() {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "set screen successfully", Toast.LENGTH_SHORT).show()
                    lastResponseValue.text = getString(R.string.set_screen_success)
                }
            }
        }

        disconnectDisplayBtn.setOnClickListener {
            pgManager.disconnectDisplay()
        }

        sendTestScreenBtn.setOnClickListener {
            val templateId = "PG2"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    data = PgScreenData(templateId, templateFields),
                    callback = loggingCallback
            )
        }

        sendPartialRefreshTestScreenBtn.setOnClickListener {
            val templateId = "PG3"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    data = PgScreenData(templateId, templateFields, RefreshType.PARTIAL_REFRESH),
                    callback = loggingCallback
            )
        }

        sendNotificationTestScreenBtn.setOnClickListener {
            val templateId = "PG2"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setNotificationScreen(
                    data = PgScreenData("PG2", templateFields),
                    callback = loggingCallback,
                    durationMs = 3000
            )
        }

        sendTestScreenBtnFailing.setOnClickListener {
            pgManager.setScreen(
                    data = PgScreenData(
                            "PG1",
                            listOf(
                                    PgTemplateField(1, "not going to be displayed", "not going to be displayed"),
                                    PgTemplateField(2, "not going to be displayed", "not going to be displayed"),
                                    PgTemplateField(3, "not going to be displayed", "not going to be displayed"),
                                    PgTemplateField(4, "not going to be displayed", "not going to be displayed")
                            )
                    ),
                    callback = loggingCallback
            )
        }

        sendPg1TestScreenBtn.setOnClickListener {
            val templateId = "PG1"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    PgScreenData(templateId, templateFields).toCommand(),
                    loggingCallback
            )
        }

        sendPg1ATestScreenBtn.setOnClickListener {
            val templateId = "PG1A"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                PgTemplateField(index + 1, pair.first, pair.second.random())
            }
            pgManager.setScreen(
                    PgScreenData(templateId, templateFields).toCommand(),
                    loggingCallback
            )
        }

        sendPg3WithRightHeadersTestScreenBtn.setOnClickListener {
            val templateId = "PG3"
            val templateFields = getSampleDataForTemplate(templateId).mapIndexed { index, pair ->
                val rightHeader = DisplaySampleData.SAMPLE_RIGHT_HEADERS.random()
                PgTemplateField(index + 1, pair.first, pair.second.random(), rightHeader)
            }
            pgManager.setScreen(
                    PgScreenData(templateId, templateFields).toCommand(),
                    loggingCallback
            )
        }
    }

    private fun getSampleDataForTemplate(template: String): List<Pair<String, Array<String>>> {
        return when (template) {
            "PG2" -> listOf(
                    DisplaySampleData.SAMPLE_STORAGE_UNIT,
                    DisplaySampleData.SAMPLE_DESTINATION
            )
            "PG3" -> listOf(
                    DisplaySampleData.SAMPLE_STORAGE_UNIT,
                    DisplaySampleData.SAMPLE_ITEM,
                    DisplaySampleData.SAMPLE_QUANTITY
            )
            "PG1I" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG1E" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG1C" -> listOf(DisplaySampleData.SAMPLE_ITEM)
            "PG2I" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG2E" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG2C" -> listOf(DisplaySampleData.SAMPLE_ITEM, DisplaySampleData.SAMPLE_QUANTITY)
            "PG1" -> listOf(arrayOf(DisplaySampleData.SAMPLE_MESSAGES, DisplaySampleData.SAMPLE_MESSAGES_2, DisplaySampleData.SAMPLE_ITEM).random())
            "PG1A" -> listOf(DisplaySampleData.SAMPLE_MESSAGES_NO_HEADER)
            else -> listOf()
        }
    }

    private fun setDefaultImageConfigurations() {
        val imageConfig = PgImageConfig()
        jpegQualityEditText.setText(imageConfig.jpegQuality.toString())
        val defaultTimeout = DEFAULT_IMAGE_TIMEOUT
        timeoutEditText.setText(defaultTimeout.toString())
    }

    private fun setupProfilesRecycler() {
        profilesAdapter = ProfilesAdapter(
                onProfileClicked = { profileId ->
                    changeConfigProfile(profileId)
                }
        )
        profilesRecycler.adapter = profilesAdapter
        profilesRecycler.layoutManager = LinearLayoutManager(this)
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
            R.id.highResolution -> ImageResolution.RESOLUTION_1280_960
            R.id.mediumResolution -> ImageResolution.RESOLUTION_640_480
            R.id.lowResolution -> ImageResolution.RESOLUTION_320_240
            else -> ImageResolution.values()[1]
        }

        val config = PgImageConfig(quality, resolution)
        val imageCallback = object : IPgImageCallback {
            override fun onImageReceived(image: PgImage) {
                val bmp = BitmapFactory.decodeByteArray(image.bytes, 0, image.bytes.size)
                runOnUiThread {
                    imageTaken.setImageBitmap(bmp)
                    lastResponseValue.text = getString(R.string.image_success)
                }
            }

            override fun onError(error: PgError) {
                runOnUiThread {
                    Toast.makeText(this@SdkActivity, "error code is $error", Toast.LENGTH_LONG).show()
                    lastResponseValue.text = error.toString()
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
            updateDisplayConnectionUiState()
        }
    }

    private fun updateDisplayConnectionUiState() {
        when {
            serviceConnectionState != ServiceConnectionStatus.CONNECTED -> displayStateOutput.setText(R.string.display_disconnected)
            displayConnected -> displayStateOutput.setText(R.string.display_connected)
            else -> displayStateOutput.setText(R.string.display_disconnected)
        }
    }

    private fun updateScannerConnectionButtonState() {
        if (scannerConnected) {
            connectScannerPinnedBtn.setText(R.string.disconnect_scanner)
            connectScannerRegularBtn.setText(R.string.disconnect_scanner)
        } else {
            connectScannerPinnedBtn.setText(R.string.pair_scanner)
            connectScannerRegularBtn.setText(R.string.pair_scanner)
        }
    }

    private fun updateServiceConnectionButtonState() {
        when (serviceConnectionState) {
            ServiceConnectionStatus.CONNECTING -> {
                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connecting)

                connectScannerPinnedBtn.setText(R.string.pair_scanner)
                connectScannerRegularBtn.setText(R.string.pair_scanner)
            }
            ServiceConnectionStatus.CONNECTED -> {
                logger.log(Level.INFO, "Connection to ProGlove SDK Service successful.")

                serviceConnectBtn.isEnabled = false
                serviceConnectBtn.setText(R.string.service_connected)

                connectScannerPinnedBtn.setText(R.string.disconnect_scanner)
                connectScannerRegularBtn.setText(R.string.disconnect_scanner)
            }
            ServiceConnectionStatus.DISCONNECTED -> {
                serviceConnectBtn.isEnabled = true
                serviceConnectBtn.setText(R.string.connect_service)

                connectScannerPinnedBtn.setText(R.string.pair_scanner)
                connectScannerRegularBtn.setText(R.string.pair_scanner)
            }
        }
    }

    private fun changeConfigProfile(profileId: String) {
        pgManager.changeConfigProfile(
                PgCommand(PgConfigProfile(profileId)),
                object : IPgConfigProfileCallback {
                    override fun onConfigProfileChanged(profile: PgConfigProfile) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "${profile.profileId} set successfully",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = getString(R.string.change_profile_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to set $profileId - $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = error.toString()
                        }
                    }
                }
        )
    }

    private fun getConfigProfiles() {
        pgManager.getConfigProfiles(
                object : IPgGetConfigProfilesCallback {
                    override fun onConfigProfilesReceived(profiles: Array<PgConfigProfile>) {
                        logger.log(Level.INFO, "received ${profiles.size} config profiles")
                        val uiProfiles: List<ProfileUiData> = profiles.map { profile ->
                            ProfileUiData(profile.profileId, profile.isActive)
                        }

                        runOnUiThread {
                            changeProfileLabel.visibility = if (profiles.isEmpty()) GONE else VISIBLE
                            profilesAdapter.updateProfiles(uiProfiles)
                            lastResponseValue.text = getString(R.string.get_profiles_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to get profiles - $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = error.toString()
                        }
                    }
                }
        )
    }

    private fun blockTrigger() {
        pgManager.blockPgTrigger(
                PgCommand(BlockPgTriggersParams(
                        listOf(PredefinedPgTrigger.DefaultPgTrigger),
                        listOf(PredefinedPgTrigger.DoubleClickMainPgTrigger),
                        0,
                        true)),
                object : IBlockPgTriggersCallback {
                    override fun onBlockTriggersCommandSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Blocking trigger success",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = getString(R.string.block_trigger_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to block the trigger: $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = error.toString()
                        }
                    }
                })
    }

    // Requires Insight Mobile v1.13.0+ and Scanner v2.5.0+
    private fun blockAllTriggersFor10s() {
        pgManager.blockPgTrigger(
                PgCommand(BlockPgTriggersParams(emptyList(), emptyList(), 10000, true)),
                object : IBlockPgTriggersCallback {
                    override fun onBlockTriggersCommandSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Blocking all triggers success",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = getString(R.string.block_trigger_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to block all triggers: $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = error.toString()
                        }
                    }
                })
    }

    private fun unblockTrigger() {
        pgManager.blockPgTrigger(
                PgCommand(BlockPgTriggersParams(emptyList(), emptyList(), 0, false)),
                object : IBlockPgTriggersCallback {
                    override fun onBlockTriggersCommandSuccess() {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Unblocking triggers success",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = getString(R.string.unblock_trigger_success)
                        }
                    }

                    override fun onError(error: PgError) {
                        runOnUiThread {
                            Toast.makeText(
                                    applicationContext,
                                    "Failed to unblock the trigger: $error",
                                    Toast.LENGTH_LONG
                            ).show()
                            lastResponseValue.text = error.toString()
                        }
                    }
                })
    }

    /**
     * NOTE: In order to use [PgManager.obtainDeviceVisibilityInfo] you need to have valid ProGlove License
     * imported in the Insight Mobile.
     * For more info reach out to your contact person at ProGlove.
     */
    private fun obtainDeviceVisibilityInfo() {
        pgManager.obtainDeviceVisibilityInfo(callback = object : IPgDeviceVisibilityCallback {
            override fun onDeviceVisibilityInfoObtained(deviceVisibilityInfo: DeviceVisibilityInfo) {
                // Display content of deviceVisibilityInfo
                logger.log(Level.INFO, "deviceVisibilityInfo: $deviceVisibilityInfo")
                runOnUiThread {
                    AlertDialog.Builder(this@SdkActivity).apply {
                        setTitle(R.string.device_visibility_alert_title)
                        setMessage(getString(R.string.device_visibility_alert_content,
                                deviceVisibilityInfo.serialNumber,
                                deviceVisibilityInfo.firmwareRevision,
                                deviceVisibilityInfo.batteryLevel,
                                deviceVisibilityInfo.bceRevision,
                                deviceVisibilityInfo.modelNumber,
                                deviceVisibilityInfo.manufacturer,
                                deviceVisibilityInfo.appVersion
                        ))
                    }.create().show()
                }
            }

            override fun onError(error: PgError) {
                // Handle error
                logger.log(Level.SEVERE, "Error during obtainDeviceVisibilityInfo: $error")
                runOnUiThread {
                    AlertDialog.Builder(this@SdkActivity).apply {
                        setTitle(R.string.device_visibility_alert_title)
                        setMessage(
                                getString(
                                        R.string.device_visibility_alert_content_error, error
                                ))
                    }.create().show()
                }
            }
        })
    }

    private fun setActivityGoals() {
        val activityGoals = PgActivityGoals(
                activityGoalsStepsGoalEdit.text.toString().toIntOrNull() ?: 650,
                activityGoalsScansGoalEdit.text.toString().toIntOrNull() ?: 10000,
                activityGoalsAverageScansGoalEdit.text.toString().toDoubleOrNull() ?: 1.5
        )
        pgManager.setActivityGoals(activityGoals.toCommand(), callback = object : IPgSetActivityGoalsCallback {
            override fun onSuccess() {
                logger.log(Level.INFO, "Goals set successfully.")
            }

            override fun onError(error: PgError) {
                logger.log(Level.SEVERE, "Error during setActivityGoals: $error")
                runOnUiThread {
                    Toast.makeText(
                            applicationContext,
                            "Failed to update goals: $error",
                            Toast.LENGTH_LONG
                    ).show()
                    lastResponseValue.text = error.toString()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        pgManager.unsubscribeFromScans(this)
        pgManager.unsubscribeFromDisplayEvents(this)
        pgManager.unsubscribeFromServiceEvents(this)
        pgManager.unsubscribeFromButtonPresses(this)
        pgManager.unsubscribeFromPgTriggersUnblocked(this)
        pgManager.unsubscribeFromPgScannerConfigurationChanges(this)
    }

    /*
     * IServiceOutput Implementation BEGIN
     */

    override fun onServiceConnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnectionStatus.CONNECTED
            logger.log(Level.INFO, "serviceConnectionState: $serviceConnectionState")
            updateButtonStates()
        }
    }

    override fun onServiceDisconnected() {
        runOnUiThread {
            serviceConnectionState = ServiceConnectionStatus.DISCONNECTED
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
            symbologyResult.text = barcodeScanResults.symbology ?: ""
            if (barcodeScanResults.symbology?.isNotEmpty() == true) {
                Toast.makeText(
                        this,
                        "Got barcode: ${barcodeScanResults.barcodeContent} with symbology ${barcodeScanResults.symbology}",
                        Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                        this,
                        "Got barcode: ${barcodeScanResults.barcodeContent} with no symbology",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onScannerConnected() {
        runOnUiThread {
            scannerConnected = true
            updateButtonStates()
        }
    }

    override fun onScannerDisconnected() {
        runOnUiThread {
            scannerConnected = false

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

    /*
     * IDisplayOutput Implementation:
     */

    override fun onDisplayConnected() {
        Log.i("DISPLAY", "connected")
        displayConnected = true
        updateButtonStates()
    }

    override fun onDisplayDisconnected() {
        Log.i("DISPLAY", "disconnected")
        displayConnected = false
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

    /*
     * ITriggersUnblockedOutput Implementation:
     */
    override fun onPgTriggersUnblocked() {
        runOnUiThread {
            Toast.makeText(this, "Triggers unblocked", Toast.LENGTH_SHORT).show()
        }
    }
    /*
     * End of ITriggersUnblockedOutput Implementation
     */


    /*
     * IScannerConfigurationChangeOutput Implementation:
     */
    override fun onScannerConfigurationChange(scannerConfigurationChangeResult: PgScannerConfigurationChangeResult) {
        runOnUiThread {
            var message = when (val status = scannerConfigurationChangeResult.scannerConfigurationChangeStatus) {
                is ScannerConfigurationChangeStatus.Success -> "Scanner configuration successfully changed"
                is ScannerConfigurationChangeStatus.NoScannerConfiguration -> "No scanner configuration in the profile"
                is ScannerConfigurationChangeStatus.Unknown -> "Unknown scanner configuration status"
                is ScannerConfigurationChangeStatus.Error -> "Fail to change scanner configuration, status: ${status.cause}"
            }

            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
            lastResponseValue.text = message
        }
    }
    /*
     * End of IScannerConfigurationChangeOutput Implementation
     */

    companion object {

        const val DEFAULT_IMAGE_TIMEOUT = 10000
    }
}

/**
 * Profile data for displaying on UI.
 */
data class ProfileUiData(val profileId: String, var active: Boolean)

