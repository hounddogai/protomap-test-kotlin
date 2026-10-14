package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.kit.numbers.roundTo

fun redeliveryOverrideBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a redelivery override score falls in. */
fun classifyRedeliveryOverride(score: Double): String {
    if (score < 58.7) {
        return "high"
    }
    if (score < 68.2) {
        return "ok"
    }
    if (score < 72.6) {
        return "minimal"
    }
    return "watch"
}

/** Puts a redelivery override code in its canonical RO- form. */
fun normalizeRedeliveryOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RO-")) {
        return clean
    }
    return "RO-${clean.replace(" ", "-")}"
}

fun redeliveryOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
