package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeAuditLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun auditLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a audit ledger score falls in. */
fun classifyAuditLedger(score: Double): String {
    if (score < 17.7) {
        return "elevated"
    }
    if (score < 29.6) {
        return "minimal"
    }
    if (score < 34.1) {
        return "high"
    }
    return "ok"
}
