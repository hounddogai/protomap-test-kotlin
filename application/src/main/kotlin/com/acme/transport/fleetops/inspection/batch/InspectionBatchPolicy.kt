package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.kit.numbers.roundTo

fun inspectionBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun inspectionBatchBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
