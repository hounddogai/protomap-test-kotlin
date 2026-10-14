package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a batched availability tier score falls in. */
fun classifyBatchedAvailabilityTier(score: Double): String {
    if (score < 7.4) {
        return "critical"
    }
    if (score < 63.7) {
        return "minimal"
    }
    return "medium"
}

fun batchedAvailabilityTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedAvailabilityTier(): Int {
    val policy = defaultPolicy("batched-availability-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeBatchedAvailabilityTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
