package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun servicePlanWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a service plan window score falls in. */
fun classifyServicePlanWindow(score: Double): String {
    if (score < 36.6) {
        return "ok"
    }
    if (score < 65.4) {
        return "medium"
    }
    return "critical"
}
