package com.acme.transport.crewing.dutyperiod.threshold

import com.acme.transport.kit.numbers.roundTo

fun dutyPeriodThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun dutyPeriodThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
