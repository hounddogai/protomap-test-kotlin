package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun seasonalDisputeTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun seasonalDisputeTierBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a seasonal dispute tier score falls in. */
fun classifySeasonalDisputeTier(score: Double): String {
    if (score < 21.0) {
        return "medium"
    }
    if (score < 84.3) {
        return "elevated"
    }
    return "high"
}

fun mergeSeasonalDisputeTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
