package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeFallbackRouteOffsetRuleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun fallbackRouteOffsetRuleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a fallback route offset rule score falls in. */
fun classifyFallbackRouteOffsetRule(score: Double): String {
    if (score < 18.8) {
        return "high"
    }
    if (score < 36.0) {
        return "minimal"
    }
    if (score < 63.3) {
        return "ok"
    }
    if (score < 69.1) {
        return "critical"
    }
    return "medium"
}
