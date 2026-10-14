package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalLeaseReturnPlan(): Int {
    val policy = defaultPolicy("regional-lease-return-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional lease return plan score falls in. */
fun classifyRegionalLeaseReturnPlan(score: Double): String {
    if (score < 11.6) {
        return "watch"
    }
    if (score < 93.9) {
        return "high"
    }
    if (score < 94.2) {
        return "critical"
    }
    if (score < 96.4) {
        return "low"
    }
    return "minimal"
}

fun mergeRegionalLeaseReturnPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun regionalLeaseReturnPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
