package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredBackhaulRollupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun tieredBackhaulRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
