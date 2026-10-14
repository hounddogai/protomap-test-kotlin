package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a strict fuel burn group score falls in. */
fun classifyStrictFuelBurnGroup(score: Double): String {
    if (score < 1.2) {
        return "ok"
    }
    if (score < 63.4) {
        return "critical"
    }
    if (score < 85.6) {
        return "medium"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictFuelBurnGroup(): Int {
    val policy = defaultPolicy("strict-fuel-burn-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeStrictFuelBurnGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
