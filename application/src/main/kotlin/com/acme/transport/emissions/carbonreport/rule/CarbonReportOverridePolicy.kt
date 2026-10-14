package com.acme.transport.emissions.carbonreport.rule

fun carbonReportOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a carbon report override score falls in. */
fun classifyCarbonReportOverride(score: Double): String {
    if (score < 70.5) {
        return "medium"
    }
    if (score < 75.5) {
        return "elevated"
    }
    return "watch"
}

/** Puts a carbon report override code in its canonical CRO- form. */
fun normalizeCarbonReportOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRO-")) {
        return clean
    }
    return "CRO-${clean.replace(" ", "-")}"
}
