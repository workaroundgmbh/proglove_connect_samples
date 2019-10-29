package de.proglove.example.intent.enums

/**
 * Helper class to parse connection status of a scanner or a display.
 */
@Suppress("unused")
enum class DeviceConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR,
    RECONNECTING,
    SETTING_UP,
    SEARCHING
}