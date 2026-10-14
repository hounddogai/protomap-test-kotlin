package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientServicePlanQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.65
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lenient service plan quota score falls in. */
fun classifyLenientServicePlanQuota(score: Double): String {
    if (score < 32.9) {
        return "ok"
    }
    if (score < 58.2) {
        return "medium"
    }
    if (score < 80.5) {
        return "critical"
    }
    if (score < 81.1) {
        return "watch"
    }
    return "low"
}

fun lenientServicePlanQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
