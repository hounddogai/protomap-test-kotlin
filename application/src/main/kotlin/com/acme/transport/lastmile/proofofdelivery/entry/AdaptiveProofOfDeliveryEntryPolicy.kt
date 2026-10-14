package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a adaptive proof of delivery entry score falls in. */
fun classifyAdaptiveProofOfDeliveryEntry(score: Double): String {
    if (score < 41.1) {
        return "medium"
    }
    if (score < 43.6) {
        return "watch"
    }
    return "critical"
}

fun adaptiveProofOfDeliveryEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun adaptiveProofOfDeliveryEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveProofOfDeliveryEntry(): Int {
    val policy = defaultPolicy("adaptive-proof-of-delivery-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
