package com.acme.transport.linehaul.tender.fallbacktenderbatch

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun rollingTenderAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun rollingTenderAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
