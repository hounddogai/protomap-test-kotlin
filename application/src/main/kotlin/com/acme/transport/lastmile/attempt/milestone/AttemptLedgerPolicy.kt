package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAttemptLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a attempt ledger code in its canonical AL- form. */
fun normalizeAttemptLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AL-")) {
        return clean
    }
    return "AL-${clean.replace(" ", "-")}"
}

fun attemptLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a attempt ledger score falls in. */
fun classifyAttemptLedger(score: Double): String {
    if (score < 68.0) {
        return "minimal"
    }
    if (score < 90.5) {
        return "ok"
    }
    return "critical"
}
