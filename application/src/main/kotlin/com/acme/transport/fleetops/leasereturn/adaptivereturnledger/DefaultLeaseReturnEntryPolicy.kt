package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.numbers.roundTo

/** Names the band a default lease return entry score falls in. */
fun classifyDefaultLeaseReturnEntry(score: Double): String {
    if (score < 35.6) {
        return "medium"
    }
    if (score < 60.8) {
        return "minimal"
    }
    if (score < 72.7) {
        return "elevated"
    }
    return "high"
}

fun defaultLeaseReturnEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun defaultLeaseReturnEntryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
