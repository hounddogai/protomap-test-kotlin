package com.acme.transport.safety.scorecard.tieredscorecardslot

/** Puts a scorecard quota code in its canonical SQ- form. */
fun normalizeScorecardQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SQ-")) {
        return clean
    }
    return "SQ-${clean.replace(" ", "-")}"
}

fun scorecardQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
