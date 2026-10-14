package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualNearMissProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.42
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun manualNearMissProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
