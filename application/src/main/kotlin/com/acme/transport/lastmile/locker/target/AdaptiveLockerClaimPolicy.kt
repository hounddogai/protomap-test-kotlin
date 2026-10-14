package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveLockerClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun adaptiveLockerClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
