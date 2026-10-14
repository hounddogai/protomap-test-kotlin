package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingServicePlanMetric(): Int {
    val policy = defaultPolicy("pending-service-plan-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending service plan metric score falls in. */
fun classifyPendingServicePlanMetric(score: Double): String {
    if (score < 39.2) {
        return "high"
    }
    if (score < 60.2) {
        return "critical"
    }
    if (score < 75.4) {
        return "ok"
    }
    if (score < 91.4) {
        return "elevated"
    }
    return "minimal"
}

fun mergePendingServicePlanMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun pendingServicePlanMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
