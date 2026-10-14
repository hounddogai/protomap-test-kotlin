package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictRestBreakTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun strictRestBreakTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
