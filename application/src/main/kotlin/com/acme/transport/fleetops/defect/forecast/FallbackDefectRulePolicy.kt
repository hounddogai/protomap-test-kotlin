package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.kit.numbers.roundTo

fun fallbackDefectRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun fallbackDefectRuleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
