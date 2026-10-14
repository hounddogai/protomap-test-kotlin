package com.acme.transport.freightpay.audit.rollup

import com.acme.transport.kit.numbers.roundTo

fun adaptiveAuditEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a adaptive audit estimate code in its canonical AAE- form. */
fun normalizeAdaptiveAuditEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AAE-")) {
        return clean
    }
    return "AAE-${clean.replace(" ", "-")}"
}

fun adaptiveAuditEstimateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
