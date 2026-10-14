package com.acme.transport.freightpay.freightinvoice.entry

/** Puts a freight invoice snapshot code in its canonical FIS- form. */
fun normalizeFreightInvoiceSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIS-")) {
        return clean
    }
    return "FIS-${clean.replace(" ", "-")}"
}

/** Names the band a freight invoice snapshot score falls in. */
fun classifyFreightInvoiceSnapshot(score: Double): String {
    if (score < 46.2) {
        return "high"
    }
    if (score < 77.5) {
        return "minimal"
    }
    if (score < 87.0) {
        return "watch"
    }
    return "medium"
}
