package de.proglove.example.intent.interfaces

interface IStatusOutput {

    /**
     * Used to update status of responses
     */
    fun onStatusReceived(status: String)
}