package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun batchedCrossDockEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedCrossDockEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
