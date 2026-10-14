package com.acme.transport.network.node.forecast

import com.acme.transport.kit.numbers.roundTo

/** Puts a node ledger code in its canonical NL- form. */
fun normalizeNodeLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NL-")) {
        return clean
    }
    return "NL-${clean.replace(" ", "-")}"
}

/** Names the band a node ledger score falls in. */
fun classifyNodeLedger(score: Double): String {
    if (score < 16.8) {
        return "low"
    }
    if (score < 18.3) {
        return "high"
    }
    if (score < 39.0) {
        return "medium"
    }
    return "watch"
}

fun nodeLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
