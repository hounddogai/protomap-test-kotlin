package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.numbers.roundTo

fun scheduledServiceAreaNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun scheduledServiceAreaNoticeBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
