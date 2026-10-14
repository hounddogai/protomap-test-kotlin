package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tiered capacity schedule score falls in. */
fun classifyTieredCapacitySchedule(score: Double): String {
    if (score < 47.0) {
        return "critical"
    }
    if (score < 90.2) {
        return "high"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredCapacitySchedule(): Int {
    val policy = defaultPolicy("tiered-capacity-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
