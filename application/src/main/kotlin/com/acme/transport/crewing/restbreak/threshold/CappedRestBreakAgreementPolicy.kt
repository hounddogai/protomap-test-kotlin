package com.acme.transport.crewing.restbreak.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCappedRestBreakAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a capped rest break agreement score falls in. */
fun classifyCappedRestBreakAgreement(score: Double): String {
    if (score < 30.0) {
        return "minimal"
    }
    if (score < 32.0) {
        return "critical"
    }
    if (score < 99.3) {
        return "low"
    }
    return "ok"
}
