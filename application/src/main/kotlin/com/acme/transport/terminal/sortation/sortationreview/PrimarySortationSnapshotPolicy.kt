package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.numbers.roundTo

fun primarySortationSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun primarySortationSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
