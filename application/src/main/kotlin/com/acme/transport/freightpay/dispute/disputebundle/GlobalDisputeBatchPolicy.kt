package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.numbers.roundTo

/** Puts a global dispute batch code in its canonical GDB- form. */
fun normalizeGlobalDisputeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDB-")) {
        return clean
    }
    return "GDB-${clean.replace(" ", "-")}"
}

fun globalDisputeBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun globalDisputeBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a global dispute batch score falls in. */
fun classifyGlobalDisputeBatch(score: Double): String {
    if (score < 33.5) {
        return "medium"
    }
    if (score < 64.2) {
        return "elevated"
    }
    if (score < 76.5) {
        return "ok"
    }
    return "high"
}
