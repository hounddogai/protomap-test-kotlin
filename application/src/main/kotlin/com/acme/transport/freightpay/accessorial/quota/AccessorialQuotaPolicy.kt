package com.acme.transport.freightpay.accessorial.quota

import com.acme.transport.kit.numbers.roundTo

fun accessorialQuotaBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun accessorialQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
