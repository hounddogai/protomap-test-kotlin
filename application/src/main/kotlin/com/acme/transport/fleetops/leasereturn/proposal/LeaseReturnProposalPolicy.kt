package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lease return proposal score falls in. */
fun classifyLeaseReturnProposal(score: Double): String {
    if (score < 14.9) {
        return "ok"
    }
    if (score < 34.0) {
        return "medium"
    }
    if (score < 52.6) {
        return "elevated"
    }
    if (score < 96.2) {
        return "minimal"
    }
    return "critical"
}

/** Puts a lease return proposal code in its canonical LRP- form. */
fun normalizeLeaseReturnProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRP-")) {
        return clean
    }
    return "LRP-${clean.replace(" ", "-")}"
}

fun leaseReturnProposalBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
