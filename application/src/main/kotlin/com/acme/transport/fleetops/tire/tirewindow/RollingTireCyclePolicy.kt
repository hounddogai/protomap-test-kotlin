package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.kit.numbers.roundTo

/** Puts a rolling tire cycle code in its canonical RTC- form. */
fun normalizeRollingTireCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTC-")) {
        return clean
    }
    return "RTC-${clean.replace(" ", "-")}"
}

fun rollingTireCycleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
