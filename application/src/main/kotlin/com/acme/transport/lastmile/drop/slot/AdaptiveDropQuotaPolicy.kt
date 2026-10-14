package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.numbers.roundTo

fun adaptiveDropQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun adaptiveDropQuotaBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a adaptive drop quota code in its canonical ADQ- form. */
fun normalizeAdaptiveDropQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADQ-")) {
        return clean
    }
    return "ADQ-${clean.replace(" ", "-")}"
}
