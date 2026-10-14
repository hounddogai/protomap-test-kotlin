package com.acme.transport.network.node.bundle

import com.acme.transport.kit.numbers.roundTo

/** Puts a node proposal code in its canonical NP- form. */
fun normalizeNodeProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NP-")) {
        return clean
    }
    return "NP-${clean.replace(" ", "-")}"
}

fun nodeProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun nodeProposalBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
