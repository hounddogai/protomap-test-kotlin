package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.numbers.roundTo

fun fallbackRedeliverySnapshotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun fallbackRedeliverySnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
