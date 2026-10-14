package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveInspectionEstimate(): Int {
    val policy = defaultPolicy("adaptive-inspection-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a adaptive inspection estimate score falls in. */
fun classifyAdaptiveInspectionEstimate(score: Double): String {
    if (score < 12.4) {
        return "ok"
    }
    if (score < 48.4) {
        return "high"
    }
    if (score < 93.2) {
        return "elevated"
    }
    return "watch"
}

fun adaptiveInspectionEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveInspectionEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
