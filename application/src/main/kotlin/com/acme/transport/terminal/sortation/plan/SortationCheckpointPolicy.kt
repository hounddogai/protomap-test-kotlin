package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.numbers.roundTo

fun sortationCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun sortationCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
