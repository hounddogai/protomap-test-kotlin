package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergePendingAvailabilityTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending availability tier score falls in. */
fun classifyPendingAvailabilityTier(score: Double): String {
    if (score < 18.2) {
        return "ok"
    }
    if (score < 46.8) {
        return "high"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingAvailabilityTier(): Int {
    val policy = defaultPolicy("pending-availability-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun pendingAvailabilityTierBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
