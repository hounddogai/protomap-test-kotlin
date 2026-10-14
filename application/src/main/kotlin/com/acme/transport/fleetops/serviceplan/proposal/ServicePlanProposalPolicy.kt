package com.acme.transport.fleetops.serviceplan.proposal

import com.acme.transport.kit.numbers.roundTo

/** Names the band a service plan proposal score falls in. */
fun classifyServicePlanProposal(score: Double): String {
    if (score < 21.3) {
        return "minimal"
    }
    if (score < 70.2) {
        return "ok"
    }
    return "watch"
}

/** Puts a service plan proposal code in its canonical SPP- form. */
fun normalizeServicePlanProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPP-")) {
        return clean
    }
    return "SPP-${clean.replace(" ", "-")}"
}

fun servicePlanProposalBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
