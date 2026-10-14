package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun primaryServiceAreaRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun primaryServiceAreaRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
