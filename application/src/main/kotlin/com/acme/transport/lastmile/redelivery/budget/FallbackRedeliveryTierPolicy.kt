package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.numbers.roundTo

fun fallbackRedeliveryTierBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a fallback redelivery tier code in its canonical FRT- form. */
fun normalizeFallbackRedeliveryTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FRT-")) {
        return clean
    }
    return "FRT-${clean.replace(" ", "-")}"
}

fun fallbackRedeliveryTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
