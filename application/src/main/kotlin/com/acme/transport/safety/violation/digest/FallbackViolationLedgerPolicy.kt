package com.acme.transport.safety.violation.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a fallback violation ledger score falls in. */
fun classifyFallbackViolationLedger(score: Double): String {
    if (score < 3.3) {
        return "watch"
    }
    if (score < 11.1) {
        return "minimal"
    }
    if (score < 83.7) {
        return "high"
    }
    return "ok"
}

fun fallbackViolationLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun mergeFallbackViolationLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
