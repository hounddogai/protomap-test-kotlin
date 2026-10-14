package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeDefaultCoachingClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultCoachingClaim(): Int {
    val policy = defaultPolicy("default-coaching-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a default coaching claim score falls in. */
fun classifyDefaultCoachingClaim(score: Double): String {
    if (score < 59.9) {
        return "minimal"
    }
    if (score < 82.3) {
        return "elevated"
    }
    return "high"
}
