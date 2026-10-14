package com.acme.transport.network.node.draft

import com.acme.transport.kit.numbers.roundTo

fun primaryNodeSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun primaryNodeSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
