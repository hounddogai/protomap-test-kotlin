package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.numbers.roundTo

fun defaultCapacityTargetBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun defaultCapacityTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
