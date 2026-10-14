package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.numbers.roundTo

fun weightedEmissionFactorThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun weightedEmissionFactorThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a weighted emission factor threshold code in its canonical WEFT- form. */
fun normalizeWeightedEmissionFactorThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WEFT-")) {
        return clean
    }
    return "WEFT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 28567.0, in order. */
fun weightedEmissionFactorThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28567.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
