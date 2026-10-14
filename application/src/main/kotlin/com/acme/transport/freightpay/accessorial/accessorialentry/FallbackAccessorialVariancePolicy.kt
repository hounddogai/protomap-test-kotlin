package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeFallbackAccessorialVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a fallback accessorial variance score falls in. */
fun classifyFallbackAccessorialVariance(score: Double): String {
    if (score < 13.5) {
        return "watch"
    }
    if (score < 30.8) {
        return "elevated"
    }
    if (score < 40.2) {
        return "minimal"
    }
    if (score < 92.2) {
        return "critical"
    }
    return "low"
}

fun fallbackAccessorialVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
