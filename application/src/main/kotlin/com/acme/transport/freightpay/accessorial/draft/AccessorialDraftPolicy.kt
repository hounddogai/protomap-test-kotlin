package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.numbers.roundTo

fun accessorialDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun accessorialDraftBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
