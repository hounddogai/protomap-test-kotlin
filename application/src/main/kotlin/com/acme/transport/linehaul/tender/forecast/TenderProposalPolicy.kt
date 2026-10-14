package com.acme.transport.linehaul.tender.forecast

/** Names the band a tender proposal score falls in. */
fun classifyTenderProposal(score: Double): String {
    if (score < 22.7) {
        return "high"
    }
    if (score < 63.7) {
        return "medium"
    }
    return "elevated"
}

/** Puts a tender proposal code in its canonical TP- form. */
fun normalizeTenderProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TP-")) {
        return clean
    }
    return "TP-${clean.replace(" ", "-")}"
}
