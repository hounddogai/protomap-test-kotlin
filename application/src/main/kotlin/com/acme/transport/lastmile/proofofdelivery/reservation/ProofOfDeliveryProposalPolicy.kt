package com.acme.transport.lastmile.proofofdelivery.reservation

/** Puts a proof of delivery proposal code in its canonical PODP- form. */
fun normalizeProofOfDeliveryProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PODP-")) {
        return clean
    }
    return "PODP-${clean.replace(" ", "-")}"
}

fun proofOfDeliveryProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a proof of delivery proposal score falls in. */
fun classifyProofOfDeliveryProposal(score: Double): String {
    if (score < 26.0) {
        return "low"
    }
    if (score < 31.2) {
        return "ok"
    }
    return "minimal"
}
