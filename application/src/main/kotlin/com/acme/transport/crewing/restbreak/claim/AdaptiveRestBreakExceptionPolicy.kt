package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.numbers.roundTo

/** Names the band a adaptive rest break exception score falls in. */
fun classifyAdaptiveRestBreakException(score: Double): String {
    if (score < 22.4) {
        return "watch"
    }
    if (score < 30.6) {
        return "ok"
    }
    if (score < 39.0) {
        return "low"
    }
    if (score < 64.5) {
        return "medium"
    }
    return "elevated"
}

fun adaptiveRestBreakExceptionBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
