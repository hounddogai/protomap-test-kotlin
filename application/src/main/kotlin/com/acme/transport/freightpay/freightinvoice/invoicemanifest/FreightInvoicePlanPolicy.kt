package com.acme.transport.freightpay.freightinvoice.invoicemanifest

fun freightInvoicePlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a freight invoice plan score falls in. */
fun classifyFreightInvoicePlan(score: Double): String {
    if (score < 3.4) {
        return "watch"
    }
    if (score < 56.6) {
        return "elevated"
    }
    if (score < 61.1) {
        return "high"
    }
    return "ok"
}

/** Puts a freight invoice plan code in its canonical FIP- form. */
fun normalizeFreightInvoicePlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIP-")) {
        return clean
    }
    return "FIP-${clean.replace(" ", "-")}"
}
