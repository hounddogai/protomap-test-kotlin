package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a seasonal inspection bundle score falls in. */
fun classifySeasonalInspectionBundle(score: Double): String {
    if (score < 39.1) {
        return "ok"
    }
    if (score < 43.1) {
        return "high"
    }
    if (score < 92.8) {
        return "low"
    }
    if (score < 94.5) {
        return "watch"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalInspectionBundle(): Int {
    val policy = defaultPolicy("seasonal-inspection-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeSeasonalInspectionBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun seasonalInspectionBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
