package com.acme.transport.emissions.fuelburn.profile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a fuel burn tier score falls in. */
fun classifyFuelBurnTier(score: Double): String {
    if (score < 4.4) {
        return "critical"
    }
    if (score < 38.3) {
        return "high"
    }
    if (score < 67.4) {
        return "watch"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnTier(): Int {
    val policy = defaultPolicy("fuel-burn-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeFuelBurnTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
