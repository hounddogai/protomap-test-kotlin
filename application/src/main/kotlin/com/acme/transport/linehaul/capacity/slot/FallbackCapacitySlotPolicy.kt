package com.acme.transport.linehaul.capacity.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a fallback capacity slot score falls in. */
fun classifyFallbackCapacitySlot(score: Double): String {
    if (score < 6.3) {
        return "low"
    }
    if (score < 30.1) {
        return "minimal"
    }
    if (score < 62.6) {
        return "medium"
    }
    if (score < 62.9) {
        return "critical"
    }
    return "elevated"
}

fun mergeFallbackCapacitySlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fallbackCapacitySlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.18
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
