package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.numbers.roundTo

fun strictIdlingSegmentBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun strictIdlingSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
