package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun adaptiveIdlingSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveIdlingSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a adaptive idling snapshot score falls in. */
fun classifyAdaptiveIdlingSnapshot(score: Double): String {
    if (score < 23.1) {
        return "low"
    }
    if (score < 38.5) {
        return "watch"
    }
    if (score < 76.7) {
        return "elevated"
    }
    return "minimal"
}
