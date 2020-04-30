package de.proglove.example.common

/**
 * This data object holds some sample data for standard picking use-cases.
 * every object is a [Pair] of type [String] to [Array] of Strings.
 * The String represents a human readable title for the values in the array.
 */
object DisplaySampleData {

    val SAMPLE_STORAGE_UNIT = "Storage Unit" to arrayOf(
            "01-005-016",
            "678",
            "R15"
    )

    val SAMPLE_DESTINATION = "Destination" to arrayOf(
            "MUNICH",
            "A7",
            "KLT 1505"
    )

    val SAMPLE_ITEM = "Item" to arrayOf(
            "0560 012",
            "Engine 12",
            "KLT 1505"
    )

    val SAMPLE_QUANTITY = "Quantity" to arrayOf(
            "10",
            "10000",
            "546555"
    )

    val SAMPLE_MESSAGES = "LOGIN" to arrayOf(
            "Scan to login and select a process"
    )

    val SAMPLE_MESSAGES_2 = "PICKING" to arrayOf(
            "Scan next part"
    )

    val SAMPLE_MESSAGES_NO_HEADER = "" to arrayOf(
            "Log in and scan first order to begin pick",
            "Scan part",
            "Scan order to begin"
    )
}