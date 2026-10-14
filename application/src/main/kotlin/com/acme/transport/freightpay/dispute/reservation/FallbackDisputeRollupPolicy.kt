package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeFallbackDisputeRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fallback dispute rollup score falls in. */
fun classifyFallbackDisputeRollup(score: Double): String {
    if (score < 12.8) {
        return "minimal"
    }
    if (score < 34.8) {
        return "watch"
    }
    if (score < 58.5) {
        return "ok"
    }
    if (score < 69.8) {
        return "critical"
    }
    return "elevated"
}

fun fallbackDisputeRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.95
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
