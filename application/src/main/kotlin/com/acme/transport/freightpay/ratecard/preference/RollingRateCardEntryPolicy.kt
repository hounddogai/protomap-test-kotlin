package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.numbers.roundTo

/** Puts a rolling rate card entry code in its canonical RRCE- form. */
fun normalizeRollingRateCardEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RRCE-")) {
        return clean
    }
    return "RRCE-${clean.replace(" ", "-")}"
}

/** Names the band a rolling rate card entry score falls in. */
fun classifyRollingRateCardEntry(score: Double): String {
    if (score < 16.8) {
        return "low"
    }
    if (score < 24.0) {
        return "medium"
    }
    if (score < 90.0) {
        return "high"
    }
    return "watch"
}

fun rollingRateCardEntryBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 90203.0, in order. */
fun rollingRateCardEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90203.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
