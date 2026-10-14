package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedEvChargeRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a capped ev charge record code in its canonical CECR- form. */
fun normalizeCappedEvChargeRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CECR-")) {
        return clean
    }
    return "CECR-${clean.replace(" ", "-")}"
}

fun cappedEvChargeRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
