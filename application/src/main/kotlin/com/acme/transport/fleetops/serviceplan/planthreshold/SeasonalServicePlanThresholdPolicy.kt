package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal service plan threshold score falls in. */
fun classifySeasonalServicePlanThreshold(score: Double): String {
    if (score < 1.3) {
        return "critical"
    }
    if (score < 17.8) {
        return "minimal"
    }
    if (score < 34.8) {
        return "watch"
    }
    if (score < 82.4) {
        return "elevated"
    }
    return "low"
}

fun seasonalServicePlanThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeSeasonalServicePlanThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalServicePlanThreshold(): Int {
    val policy = defaultPolicy("seasonal-service-plan-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
