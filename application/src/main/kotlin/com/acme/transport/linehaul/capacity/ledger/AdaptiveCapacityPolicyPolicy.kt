package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun adaptiveCapacityPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a adaptive capacity policy score falls in. */
fun classifyAdaptiveCapacityPolicy(score: Double): String {
    if (score < 18.4) {
        return "elevated"
    }
    if (score < 68.2) {
        return "watch"
    }
    if (score < 79.1) {
        return "low"
    }
    if (score < 80.3) {
        return "minimal"
    }
    return "critical"
}

fun adaptiveCapacityPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeAdaptiveCapacityPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
