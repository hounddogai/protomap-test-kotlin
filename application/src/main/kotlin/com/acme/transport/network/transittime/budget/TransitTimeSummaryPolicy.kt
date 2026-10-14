package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.numbers.roundTo

/** Puts a transit time summary code in its canonical TTS- form. */
fun normalizeTransitTimeSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTS-")) {
        return clean
    }
    return "TTS-${clean.replace(" ", "-")}"
}

fun transitTimeSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun transitTimeSummaryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
