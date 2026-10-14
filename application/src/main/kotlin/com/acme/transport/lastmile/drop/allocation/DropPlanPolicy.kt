package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.numbers.roundTo

fun dropPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun dropPlanBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
