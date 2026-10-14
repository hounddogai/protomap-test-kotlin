package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun trailerYardWindowBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun trailerYardWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
