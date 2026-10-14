package com.acme.transport.lastmile.locker.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lockerProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.36
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lockerProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
