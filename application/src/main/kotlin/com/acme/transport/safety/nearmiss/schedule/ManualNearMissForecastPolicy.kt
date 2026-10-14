package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a manual near miss forecast score falls in. */
fun classifyManualNearMissForecast(score: Double): String {
    if (score < 38.2) {
        return "minimal"
    }
    if (score < 56.7) {
        return "critical"
    }
    if (score < 60.5) {
        return "medium"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualNearMissForecast(): Int {
    val policy = defaultPolicy("manual-near-miss-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeManualNearMissForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun manualNearMissForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
