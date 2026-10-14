package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun relayAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a relay adjustment code in its canonical RA- form. */
fun normalizeRelayAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RA-")) {
        return clean
    }
    return "RA-${clean.replace(" ", "-")}"
}

fun relayAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
