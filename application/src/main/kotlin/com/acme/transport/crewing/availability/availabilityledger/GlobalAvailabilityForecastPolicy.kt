package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.numbers.roundTo

fun globalAvailabilityForecastBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a global availability forecast code in its canonical GAF- form. */
fun normalizeGlobalAvailabilityForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GAF-")) {
        return clean
    }
    return "GAF-${clean.replace(" ", "-")}"
}
