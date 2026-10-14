package com.acme.transport.linehaul.laneplan.plancheckpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun fallbackLanePlanCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackLanePlanCheckpoint(): Int {
    val policy = defaultPolicy("fallback-lane-plan-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeFallbackLanePlanCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fallbackLanePlanCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.25
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
