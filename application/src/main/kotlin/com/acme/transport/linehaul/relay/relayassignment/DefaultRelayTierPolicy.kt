package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.numbers.roundTo

fun defaultRelayTierBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun defaultRelayTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
