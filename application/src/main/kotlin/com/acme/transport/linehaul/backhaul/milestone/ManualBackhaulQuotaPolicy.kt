package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.numbers.roundTo

fun manualBackhaulQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun manualBackhaulQuotaBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
