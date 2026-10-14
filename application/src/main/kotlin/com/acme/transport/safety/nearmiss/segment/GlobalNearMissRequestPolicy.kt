package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalNearMissRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalNearMissRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
