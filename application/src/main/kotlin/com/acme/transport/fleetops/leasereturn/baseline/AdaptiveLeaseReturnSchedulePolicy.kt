package com.acme.transport.fleetops.leasereturn.baseline

import com.acme.transport.kit.numbers.roundTo

fun adaptiveLeaseReturnScheduleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun adaptiveLeaseReturnScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
