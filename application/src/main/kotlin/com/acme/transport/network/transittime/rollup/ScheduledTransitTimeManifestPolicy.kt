package com.acme.transport.network.transittime.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun scheduledTransitTimeManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeScheduledTransitTimeManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scheduled transit time manifest score falls in. */
fun classifyScheduledTransitTimeManifest(score: Double): String {
    if (score < 16.6) {
        return "elevated"
    }
    if (score < 51.9) {
        return "ok"
    }
    if (score < 67.6) {
        return "critical"
    }
    return "low"
}
