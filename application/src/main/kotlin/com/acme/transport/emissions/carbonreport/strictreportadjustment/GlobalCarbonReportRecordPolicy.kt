package com.acme.transport.emissions.carbonreport.strictreportadjustment

/** Names the band a global carbon report record score falls in. */
fun classifyGlobalCarbonReportRecord(score: Double): String {
    if (score < 12.1) {
        return "medium"
    }
    if (score < 62.0) {
        return "ok"
    }
    if (score < 72.2) {
        return "high"
    }
    if (score < 97.3) {
        return "critical"
    }
    return "watch"
}

/** Puts a global carbon report record code in its canonical GCRR- form. */
fun normalizeGlobalCarbonReportRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCRR-")) {
        return clean
    }
    return "GCRR-${clean.replace(" ", "-")}"
}
