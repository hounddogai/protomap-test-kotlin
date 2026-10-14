package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun weightedRateCardRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedRateCardRollupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun mergeWeightedRateCardRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a weighted rate card rollup score falls in. */
fun classifyWeightedRateCardRollup(score: Double): String {
    if (score < 10.8) {
        return "low"
    }
    if (score < 37.1) {
        return "elevated"
    }
    if (score < 86.4) {
        return "ok"
    }
    if (score < 99.5) {
        return "watch"
    }
    return "high"
}
