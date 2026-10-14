package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun pendingFuelSurchargeGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a pending fuel surcharge group score falls in. */
fun classifyPendingFuelSurchargeGroup(score: Double): String {
    if (score < 50.9) {
        return "critical"
    }
    if (score < 84.6) {
        return "minimal"
    }
    return "ok"
}

fun pendingFuelSurchargeGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
