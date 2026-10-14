package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun zoneLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeZoneLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a zone ledger code in its canonical ZL- form. */
fun normalizeZoneLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ZL-")) {
        return clean
    }
    return "ZL-${clean.replace(" ", "-")}"
}

/** Names the band a zone ledger score falls in. */
fun classifyZoneLedger(score: Double): String {
    if (score < 10.0) {
        return "critical"
    }
    if (score < 42.4) {
        return "low"
    }
    if (score < 73.7) {
        return "ok"
    }
    if (score < 78.4) {
        return "high"
    }
    return "minimal"
}
