package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.numbers.roundTo

fun manualHubQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun manualHubQuotaBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a manual hub quota code in its canonical MHQ- form. */
fun normalizeManualHubQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MHQ-")) {
        return clean
    }
    return "MHQ-${clean.replace(" ", "-")}"
}
