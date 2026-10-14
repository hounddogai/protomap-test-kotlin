package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a pending service plan preference score falls in. */
fun classifyPendingServicePlanPreference(score: Double): String {
    if (score < 46.9) {
        return "medium"
    }
    if (score < 61.0) {
        return "elevated"
    }
    return "watch"
}

fun pendingServicePlanPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingServicePlanPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
