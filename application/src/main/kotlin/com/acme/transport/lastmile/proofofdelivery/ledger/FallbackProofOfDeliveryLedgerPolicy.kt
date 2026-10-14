package com.acme.transport.lastmile.proofofdelivery.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun fallbackProofOfDeliveryLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

fun mergeFallbackProofOfDeliveryLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a fallback proof of delivery ledger code in its canonical FPODL- form. */
fun normalizeFallbackProofOfDeliveryLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FPODL-")) {
        return clean
    }
    return "FPODL-${clean.replace(" ", "-")}"
}

/** Names the band a fallback proof of delivery ledger score falls in. */
fun classifyFallbackProofOfDeliveryLedger(score: Double): String {
    if (score < 13.7) {
        return "medium"
    }
    if (score < 16.5) {
        return "elevated"
    }
    if (score < 53.2) {
        return "critical"
    }
    if (score < 89.6) {
        return "low"
    }
    return "watch"
}
