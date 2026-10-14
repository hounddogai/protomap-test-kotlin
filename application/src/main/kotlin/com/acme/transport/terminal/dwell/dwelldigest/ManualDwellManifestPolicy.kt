package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a manual dwell manifest code in its canonical MDM- form. */
fun normalizeManualDwellManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MDM-")) {
        return clean
    }
    return "MDM-${clean.replace(" ", "-")}"
}

fun manualDwellManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun manualDwellManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
