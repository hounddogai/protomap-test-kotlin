package com.acme.transport.emissions.carbonreport.score

fun pendingCarbonReportExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a pending carbon report exception code in its canonical PCRE- form. */
fun normalizePendingCarbonReportExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCRE-")) {
        return clean
    }
    return "PCRE-${clean.replace(" ", "-")}"
}
