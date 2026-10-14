package com.acme.transport.crewing.qualification.allocation

import com.acme.transport.kit.numbers.roundTo

fun rollingQualificationAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun rollingQualificationAllocationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
