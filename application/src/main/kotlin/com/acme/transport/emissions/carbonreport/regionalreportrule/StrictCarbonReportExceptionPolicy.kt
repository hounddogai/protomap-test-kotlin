package com.acme.transport.emissions.carbonreport.regionalreportrule

/** Puts a strict carbon report exception code in its canonical SCRE- form. */
fun normalizeStrictCarbonReportExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCRE-")) {
        return clean
    }
    return "SCRE-${clean.replace(" ", "-")}"
}

/** Names the band a strict carbon report exception score falls in. */
fun classifyStrictCarbonReportException(score: Double): String {
    if (score < 6.8) {
        return "elevated"
    }
    if (score < 58.5) {
        return "low"
    }
    if (score < 73.8) {
        return "minimal"
    }
    return "ok"
}
