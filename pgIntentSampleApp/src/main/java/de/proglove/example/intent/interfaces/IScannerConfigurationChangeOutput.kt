package de.proglove.example.intent.interfaces

interface IScannerConfigurationChangeOutput {

    /**
     * Used to update status of configuration responses
     */
    fun onScannerConfigurationChange(status: String, errorMessage: String?)
}