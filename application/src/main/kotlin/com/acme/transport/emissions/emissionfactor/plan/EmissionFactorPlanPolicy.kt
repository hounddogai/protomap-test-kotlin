package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeEmissionFactorPlan(): Int {
    val policy = defaultPolicy("emission-factor-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a emission factor plan score falls in. */
fun classifyEmissionFactorPlan(score: Double): String {
    if (score < 1.2) {
        return "high"
    }
    if (score < 11.4) {
        return "ok"
    }
    if (score < 35.0) {
        return "medium"
    }
    if (score < 66.9) {
        return "minimal"
    }
    return "critical"
}

fun mergeEmissionFactorPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
