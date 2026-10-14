package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.numbers.roundTo

fun cappedRelayReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun cappedRelayReviewBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
