package com.acme.transport.freightpay.freightinvoice.batch

fun defaultFreightInvoiceBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a default freight invoice batch score falls in. */
fun classifyDefaultFreightInvoiceBatch(score: Double): String {
    if (score < 31.0) {
        return "medium"
    }
    if (score < 35.3) {
        return "minimal"
    }
    if (score < 73.1) {
        return "elevated"
    }
    if (score < 79.6) {
        return "low"
    }
    return "watch"
}

/** Puts a default freight invoice batch code in its canonical DFIB- form. */
fun normalizeDefaultFreightInvoiceBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DFIB-")) {
        return clean
    }
    return "DFIB-${clean.replace(" ", "-")}"
}
