package com.acme.transport.fleetops.inspection.inspectionmapping

/** Names the band a inspection ledger score falls in. */
fun classifyInspectionLedger(score: Double): String {
    if (score < 8.8) {
        return "high"
    }
    if (score < 97.3) {
        return "critical"
    }
    return "ok"
}

/** Puts a inspection ledger code in its canonical IL- form. */
fun normalizeInspectionLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IL-")) {
        return clean
    }
    return "IL-${clean.replace(" ", "-")}"
}
