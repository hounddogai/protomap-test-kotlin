package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.numbers.roundTo

fun trailerYardTargetBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a trailer yard target score falls in. */
fun classifyTrailerYardTarget(score: Double): String {
    if (score < 9.5) {
        return "critical"
    }
    if (score < 28.9) {
        return "minimal"
    }
    if (score < 70.3) {
        return "watch"
    }
    return "ok"
}
