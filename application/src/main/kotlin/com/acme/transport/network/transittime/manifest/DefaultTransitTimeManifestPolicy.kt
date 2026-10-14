package com.acme.transport.network.transittime.manifest

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 85648.0, in order. */
fun defaultTransitTimeManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85648.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultTransitTimeManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun defaultTransitTimeManifestBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a default transit time manifest score falls in. */
fun classifyDefaultTransitTimeManifest(score: Double): String {
    if (score < 26.9) {
        return "low"
    }
    if (score < 51.1) {
        return "medium"
    }
    return "critical"
}
