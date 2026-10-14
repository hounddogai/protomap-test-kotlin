package com.acme.transport.network.cutoff.target

import com.acme.transport.kit.numbers.roundTo

fun cutoffRuleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a cutoff rule score falls in. */
fun classifyCutoffRule(score: Double): String {
    if (score < 39.3) {
        return "medium"
    }
    if (score < 52.2) {
        return "watch"
    }
    if (score < 90.4) {
        return "ok"
    }
    return "elevated"
}
