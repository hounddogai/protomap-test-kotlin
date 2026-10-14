package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedRosterTier(): Int {
    val policy = defaultPolicy("weighted-roster-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted roster tier score falls in. */
fun classifyWeightedRosterTier(score: Double): String {
    if (score < 18.7) {
        return "elevated"
    }
    if (score < 76.6) {
        return "watch"
    }
    return "critical"
}
