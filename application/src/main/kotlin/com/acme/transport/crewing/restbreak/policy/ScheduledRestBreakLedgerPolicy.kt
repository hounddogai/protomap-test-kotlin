package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun scheduledRestBreakLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun scheduledRestBreakLedgerBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
