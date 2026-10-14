package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun cutoffCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a cutoff cycle score falls in. */
fun classifyCutoffCycle(score: Double): String {
    if (score < 3.4) {
        return "minimal"
    }
    if (score < 12.9) {
        return "elevated"
    }
    if (score < 30.6) {
        return "high"
    }
    return "critical"
}

fun mergeCutoffCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
