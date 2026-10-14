package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun manualBackhaulAuditBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a manual backhaul audit code in its canonical MBA- form. */
fun normalizeManualBackhaulAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MBA-")) {
        return clean
    }
    return "MBA-${clean.replace(" ", "-")}"
}

fun manualBackhaulAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.24
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
