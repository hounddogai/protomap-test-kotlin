package com.acme.transport.customs.tariff.entry

import com.acme.transport.kit.numbers.roundTo

fun tariffSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun tariffSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
