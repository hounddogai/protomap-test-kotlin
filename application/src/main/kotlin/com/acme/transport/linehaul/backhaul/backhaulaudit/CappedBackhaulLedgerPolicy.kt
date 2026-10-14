package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun cappedBackhaulLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun cappedBackhaulLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedBackhaulLedgerBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
