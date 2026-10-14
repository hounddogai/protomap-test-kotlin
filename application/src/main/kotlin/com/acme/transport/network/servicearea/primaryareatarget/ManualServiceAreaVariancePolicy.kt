package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualServiceAreaVariance(): Int {
    val policy = defaultPolicy("manual-service-area-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual service area variance score falls in. */
fun classifyManualServiceAreaVariance(score: Double): String {
    if (score < 10.7) {
        return "watch"
    }
    if (score < 14.4) {
        return "elevated"
    }
    if (score < 57.1) {
        return "high"
    }
    if (score < 61.8) {
        return "minimal"
    }
    return "low"
}

fun manualServiceAreaVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeManualServiceAreaVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
