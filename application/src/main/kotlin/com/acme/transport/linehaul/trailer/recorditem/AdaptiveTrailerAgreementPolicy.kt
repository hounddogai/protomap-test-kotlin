package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun adaptiveTrailerAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive trailer agreement score falls in. */
fun classifyAdaptiveTrailerAgreement(score: Double): String {
    if (score < 10.5) {
        return "critical"
    }
    if (score < 13.3) {
        return "low"
    }
    if (score < 91.0) {
        return "watch"
    }
    return "minimal"
}

fun adaptiveTrailerAgreementBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeAdaptiveTrailerAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
