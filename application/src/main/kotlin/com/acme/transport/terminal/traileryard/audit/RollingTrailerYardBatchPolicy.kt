package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingTrailerYardBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun rollingTrailerYardBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
