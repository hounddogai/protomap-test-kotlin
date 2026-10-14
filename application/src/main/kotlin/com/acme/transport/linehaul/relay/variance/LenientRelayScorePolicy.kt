package com.acme.transport.linehaul.relay.variance

import com.acme.transport.kit.numbers.roundTo

fun lenientRelayScoreBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a lenient relay score score falls in. */
fun classifyLenientRelayScore(score: Double): String {
    if (score < 48.4) {
        return "high"
    }
    if (score < 72.9) {
        return "ok"
    }
    return "medium"
}
