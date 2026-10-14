package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.numbers.roundTo

fun fallbackIdlingVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun fallbackIdlingVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
