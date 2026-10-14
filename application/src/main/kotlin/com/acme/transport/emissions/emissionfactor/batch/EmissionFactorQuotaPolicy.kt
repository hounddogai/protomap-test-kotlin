package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a emission factor quota score falls in. */
fun classifyEmissionFactorQuota(score: Double): String {
    if (score < 18.0) {
        return "low"
    }
    if (score < 26.8) {
        return "critical"
    }
    if (score < 62.2) {
        return "minimal"
    }
    if (score < 97.2) {
        return "medium"
    }
    return "high"
}

fun emissionFactorQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun emissionFactorQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeEmissionFactorQuotaLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
