package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a lenient availability manifest code in its canonical LAM- form. */
fun normalizeLenientAvailabilityManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAM-")) {
        return clean
    }
    return "LAM-${clean.replace(" ", "-")}"
}

fun lenientAvailabilityManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun lenientAvailabilityManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.08
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
