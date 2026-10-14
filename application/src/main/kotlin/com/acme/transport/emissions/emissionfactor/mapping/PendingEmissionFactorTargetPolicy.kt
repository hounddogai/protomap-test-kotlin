package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingEmissionFactorTarget(): Int {
    val policy = defaultPolicy("pending-emission-factor-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergePendingEmissionFactorTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending emission factor target score falls in. */
fun classifyPendingEmissionFactorTarget(score: Double): String {
    if (score < 7.2) {
        return "medium"
    }
    if (score < 14.8) {
        return "elevated"
    }
    if (score < 67.4) {
        return "critical"
    }
    if (score < 96.5) {
        return "minimal"
    }
    return "high"
}
