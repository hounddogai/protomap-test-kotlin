package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a near miss threshold code in its canonical NMT- form. */
fun normalizeNearMissThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NMT-")) {
        return clean
    }
    return "NMT-${clean.replace(" ", "-")}"
}

fun nearMissThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun nearMissThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
