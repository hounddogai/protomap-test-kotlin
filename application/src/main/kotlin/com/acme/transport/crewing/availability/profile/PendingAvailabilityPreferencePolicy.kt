package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a pending availability preference code in its canonical PAP- form. */
fun normalizePendingAvailabilityPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAP-")) {
        return clean
    }
    return "PAP-${clean.replace(" ", "-")}"
}

fun pendingAvailabilityPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingAvailabilityPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
