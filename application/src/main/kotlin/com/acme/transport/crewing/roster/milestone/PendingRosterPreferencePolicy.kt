package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.numbers.roundTo

fun pendingRosterPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun pendingRosterPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
