package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun servicePlanNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun servicePlanNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
