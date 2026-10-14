package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 44743.0, in order. */
fun cappedHubRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44743.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedHubRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a capped hub request score falls in. */
fun classifyCappedHubRequest(score: Double): String {
    if (score < 15.2) {
        return "ok"
    }
    if (score < 25.1) {
        return "high"
    }
    if (score < 67.4) {
        return "elevated"
    }
    return "medium"
}

fun cappedHubRequestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
