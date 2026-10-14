package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun adaptiveServicePlanManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveServicePlanManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun mergeAdaptiveServicePlanManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive service plan manifest score falls in. */
fun classifyAdaptiveServicePlanManifest(score: Double): String {
    if (score < 3.0) {
        return "medium"
    }
    if (score < 30.8) {
        return "high"
    }
    if (score < 97.2) {
        return "low"
    }
    return "critical"
}
