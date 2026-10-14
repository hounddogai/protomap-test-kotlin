package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.numbers.roundTo

/** Puts a regional broker metric code in its canonical RBM- form. */
fun normalizeRegionalBrokerMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBM-")) {
        return clean
    }
    return "RBM-${clean.replace(" ", "-")}"
}

fun regionalBrokerMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun regionalBrokerMetricBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
