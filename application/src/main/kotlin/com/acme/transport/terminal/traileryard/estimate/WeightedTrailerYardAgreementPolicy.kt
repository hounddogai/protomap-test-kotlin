package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a weighted trailer yard agreement score falls in. */
fun classifyWeightedTrailerYardAgreement(score: Double): String {
    if (score < 13.7) {
        return "low"
    }
    if (score < 29.0) {
        return "ok"
    }
    if (score < 47.0) {
        return "medium"
    }
    if (score < 75.3) {
        return "high"
    }
    return "critical"
}

fun mergeWeightedTrailerYardAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedTrailerYardAgreementBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
