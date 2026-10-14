package com.acme.transport.safety.scorecard.scorecardproposal

fun manualScorecardProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a manual scorecard proposal code in its canonical MSP- form. */
fun normalizeManualScorecardProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MSP-")) {
        return clean
    }
    return "MSP-${clean.replace(" ", "-")}"
}
