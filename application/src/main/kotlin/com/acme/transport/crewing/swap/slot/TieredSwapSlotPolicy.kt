package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a tiered swap slot score falls in. */
fun classifyTieredSwapSlot(score: Double): String {
    if (score < 25.2) {
        return "minimal"
    }
    if (score < 89.5) {
        return "high"
    }
    return "ok"
}

fun mergeTieredSwapSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun tieredSwapSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
