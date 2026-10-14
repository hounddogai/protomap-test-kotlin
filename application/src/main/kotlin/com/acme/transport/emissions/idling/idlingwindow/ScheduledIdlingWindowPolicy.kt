package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun scheduledIdlingWindowBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun scheduledIdlingWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun scheduledIdlingWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
