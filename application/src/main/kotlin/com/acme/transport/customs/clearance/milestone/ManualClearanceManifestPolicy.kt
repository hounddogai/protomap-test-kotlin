package com.acme.transport.customs.clearance.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun manualClearanceManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun manualClearanceManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a manual clearance manifest code in its canonical MCM- form. */
fun normalizeManualClearanceManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MCM-")) {
        return clean
    }
    return "MCM-${clean.replace(" ", "-")}"
}
