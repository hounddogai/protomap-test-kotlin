package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 45001.0, in order. */
fun seasonalTransitTimeCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 45001.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal transit time checkpoint code in its canonical STTC- form. */
fun normalizeSeasonalTransitTimeCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STTC-")) {
        return clean
    }
    return "STTC-${clean.replace(" ", "-")}"
}

fun seasonalTransitTimeCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
