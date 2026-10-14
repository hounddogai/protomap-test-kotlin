package com.acme.transport.network.node.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun primaryNodeVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a primary node variance score falls in. */
fun classifyPrimaryNodeVariance(score: Double): String {
    if (score < 4.8) {
        return "high"
    }
    if (score < 12.5) {
        return "ok"
    }
    return "critical"
}

fun mergePrimaryNodeVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a primary node variance code in its canonical PNV- form. */
fun normalizePrimaryNodeVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNV-")) {
        return clean
    }
    return "PNV-${clean.replace(" ", "-")}"
}
