package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultHubSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun defaultHubSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Puts a default hub summary code in its canonical DHS- form. */
fun normalizeDefaultHubSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DHS-")) {
        return clean
    }
    return "DHS-${clean.replace(" ", "-")}"
}
