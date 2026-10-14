package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLegacyEvChargeVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy ev charge variance score falls in. */
fun classifyLegacyEvChargeVariance(score: Double): String {
    if (score < 13.9) {
        return "watch"
    }
    if (score < 20.1) {
        return "critical"
    }
    if (score < 28.8) {
        return "ok"
    }
    if (score < 55.0) {
        return "high"
    }
    return "minimal"
}
