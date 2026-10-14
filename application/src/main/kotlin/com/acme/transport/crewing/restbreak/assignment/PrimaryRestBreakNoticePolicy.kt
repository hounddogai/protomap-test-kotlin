package com.acme.transport.crewing.restbreak.assignment

import com.acme.transport.kit.numbers.roundTo

/** Names the band a primary rest break notice score falls in. */
fun classifyPrimaryRestBreakNotice(score: Double): String {
    if (score < 40.4) {
        return "ok"
    }
    if (score < 42.9) {
        return "critical"
    }
    if (score < 46.8) {
        return "high"
    }
    if (score < 74.8) {
        return "watch"
    }
    return "medium"
}

fun primaryRestBreakNoticeBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
