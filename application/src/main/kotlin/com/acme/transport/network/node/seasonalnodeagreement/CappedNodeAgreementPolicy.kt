package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a capped node agreement score falls in. */
fun classifyCappedNodeAgreement(score: Double): String {
    if (score < 2.2) {
        return "watch"
    }
    if (score < 10.6) {
        return "critical"
    }
    if (score < 93.7) {
        return "high"
    }
    if (score < 99.8) {
        return "minimal"
    }
    return "elevated"
}

fun mergeCappedNodeAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a capped node agreement code in its canonical CNA- form. */
fun normalizeCappedNodeAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CNA-")) {
        return clean
    }
    return "CNA-${clean.replace(" ", "-")}"
}

fun cappedNodeAgreementBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
