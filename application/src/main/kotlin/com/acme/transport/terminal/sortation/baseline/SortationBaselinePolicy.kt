package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.kit.numbers.roundTo

fun sortationBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun sortationBaselineBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
