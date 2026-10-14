package com.acme.transport.freightpay.freightinvoice.threshold

fun regionalFreightInvoiceThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a regional freight invoice threshold code in its canonical RFIT- form. */
fun normalizeRegionalFreightInvoiceThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFIT-")) {
        return clean
    }
    return "RFIT-${clean.replace(" ", "-")}"
}

/** Names the band a regional freight invoice threshold score falls in. */
fun classifyRegionalFreightInvoiceThreshold(score: Double): String {
    if (score < 60.1) {
        return "high"
    }
    if (score < 73.5) {
        return "low"
    }
    if (score < 94.1) {
        return "medium"
    }
    if (score < 96.4) {
        return "elevated"
    }
    return "minimal"
}
