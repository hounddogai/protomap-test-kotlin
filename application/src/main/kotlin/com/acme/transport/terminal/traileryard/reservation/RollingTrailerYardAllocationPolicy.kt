package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingTrailerYardAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingTrailerYardAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
