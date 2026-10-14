package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.numbers.roundTo

/** Names the band a global node variance score falls in. */
fun classifyGlobalNodeVariance(score: Double): String {
    if (score < 6.2) {
        return "ok"
    }
    if (score < 13.6) {
        return "watch"
    }
    return "critical"
}

/** Puts a global node variance code in its canonical GNV- form. */
fun normalizeGlobalNodeVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GNV-")) {
        return clean
    }
    return "GNV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 47946.0, in order. */
fun globalNodeVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 47946.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalNodeVarianceBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
