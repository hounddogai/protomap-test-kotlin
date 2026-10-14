package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.numbers.roundTo

fun regionalRedeliveryPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun regionalRedeliveryPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
