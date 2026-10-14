package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun globalRedeliveryBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalRedeliveryBundleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a global redelivery bundle score falls in. */
fun classifyGlobalRedeliveryBundle(score: Double): String {
    if (score < 5.0) {
        return "watch"
    }
    if (score < 37.2) {
        return "critical"
    }
    if (score < 53.9) {
        return "low"
    }
    if (score < 76.2) {
        return "elevated"
    }
    return "ok"
}
