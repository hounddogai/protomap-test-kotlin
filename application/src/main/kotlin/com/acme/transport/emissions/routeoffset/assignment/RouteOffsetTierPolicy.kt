package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.numbers.roundTo

fun routeOffsetTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun routeOffsetTierBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
