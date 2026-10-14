package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun legacyDisputeProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeLegacyDisputeProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy dispute profile score falls in. */
fun classifyLegacyDisputeProfile(score: Double): String {
    if (score < 53.4) {
        return "high"
    }
    if (score < 69.9) {
        return "watch"
    }
    if (score < 81.0) {
        return "low"
    }
    return "elevated"
}

fun legacyDisputeProfileBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
