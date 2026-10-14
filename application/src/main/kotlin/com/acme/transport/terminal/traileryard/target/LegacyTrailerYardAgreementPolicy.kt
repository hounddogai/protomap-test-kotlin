package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun legacyTrailerYardAgreementBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeLegacyTrailerYardAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a legacy trailer yard agreement score falls in. */
fun classifyLegacyTrailerYardAgreement(score: Double): String {
    if (score < 20.2) {
        return "medium"
    }
    if (score < 62.8) {
        return "critical"
    }
    return "ok"
}
