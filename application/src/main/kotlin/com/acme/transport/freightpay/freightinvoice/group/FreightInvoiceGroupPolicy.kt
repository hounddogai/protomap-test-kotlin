package com.acme.transport.freightpay.freightinvoice.group

/** Names the band a freight invoice group score falls in. */
fun classifyFreightInvoiceGroup(score: Double): String {
    if (score < 45.0) {
        return "ok"
    }
    if (score < 60.2) {
        return "low"
    }
    if (score < 72.1) {
        return "elevated"
    }
    if (score < 82.9) {
        return "medium"
    }
    return "watch"
}

/** Puts a freight invoice group code in its canonical FIG- form. */
fun normalizeFreightInvoiceGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIG-")) {
        return clean
    }
    return "FIG-${clean.replace(" ", "-")}"
}
