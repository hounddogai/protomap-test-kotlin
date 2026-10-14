package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.numbers.roundTo

fun lenientAccessorialAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a lenient accessorial allocation score falls in. */
fun classifyLenientAccessorialAllocation(score: Double): String {
    if (score < 3.3) {
        return "minimal"
    }
    if (score < 31.4) {
        return "medium"
    }
    return "elevated"
}

fun lenientAccessorialAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
