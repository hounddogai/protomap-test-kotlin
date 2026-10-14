package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a fallback inspection policy score falls in. */
fun classifyFallbackInspectionPolicy(score: Double): String {
    if (score < 21.8) {
        return "elevated"
    }
    if (score < 48.5) {
        return "watch"
    }
    if (score < 74.4) {
        return "low"
    }
    if (score < 79.7) {
        return "ok"
    }
    return "medium"
}

fun mergeFallbackInspectionPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fallbackInspectionPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
