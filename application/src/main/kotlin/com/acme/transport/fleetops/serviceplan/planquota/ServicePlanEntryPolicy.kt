package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServicePlanEntry(): Int {
    val policy = defaultPolicy("service-plan-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeServicePlanEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a service plan entry score falls in. */
fun classifyServicePlanEntry(score: Double): String {
    if (score < 9.2) {
        return "elevated"
    }
    if (score < 40.5) {
        return "minimal"
    }
    if (score < 54.3) {
        return "low"
    }
    return "high"
}
