package com.acme.transport.fleetops.tire.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tire preference code in its canonical TP- form. */
fun normalizeTirePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TP-")) {
        return clean
    }
    return "TP-${clean.replace(" ", "-")}"
}

fun tirePreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tirePreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
