package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.numbers.roundTo

fun seasonalRedeliverySlotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun seasonalRedeliverySlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
