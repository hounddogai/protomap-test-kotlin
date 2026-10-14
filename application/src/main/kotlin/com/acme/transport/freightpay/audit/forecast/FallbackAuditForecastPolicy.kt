package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackAuditForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun fallbackAuditForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
