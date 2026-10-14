package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.numbers.roundTo

fun seasonalAccessorialScoreBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun seasonalAccessorialScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
