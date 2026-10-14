package com.acme.transport.linehaul.laneplan.quota

/** Puts a rolling lane plan ledger code in its canonical RLPL- form. */
fun normalizeRollingLanePlanLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RLPL-")) {
        return clean
    }
    return "RLPL-${clean.replace(" ", "-")}"
}

fun rollingLanePlanLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
