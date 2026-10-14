package com.acme.transport.terminal.dwell.dwellpreference

fun dwellLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a dwell ledger code in its canonical DL- form. */
fun normalizeDwellLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DL-")) {
        return clean
    }
    return "DL-${clean.replace(" ", "-")}"
}

/** Names the band a dwell ledger score falls in. */
fun classifyDwellLedger(score: Double): String {
    if (score < 27.3) {
        return "minimal"
    }
    if (score < 49.0) {
        return "critical"
    }
    return "ok"
}
