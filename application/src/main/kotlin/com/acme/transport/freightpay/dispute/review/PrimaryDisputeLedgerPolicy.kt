package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a primary dispute ledger code in its canonical PDL- form. */
fun normalizePrimaryDisputeLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDL-")) {
        return clean
    }
    return "PDL-${clean.replace(" ", "-")}"
}

/** Names the band a primary dispute ledger score falls in. */
fun classifyPrimaryDisputeLedger(score: Double): String {
    if (score < 49.8) {
        return "critical"
    }
    if (score < 53.2) {
        return "watch"
    }
    return "high"
}

fun mergePrimaryDisputeLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryDisputeLedgerBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
