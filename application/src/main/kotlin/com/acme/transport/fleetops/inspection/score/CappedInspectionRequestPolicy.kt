package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.numbers.roundTo

fun cappedInspectionRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

fun cappedInspectionRequestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
