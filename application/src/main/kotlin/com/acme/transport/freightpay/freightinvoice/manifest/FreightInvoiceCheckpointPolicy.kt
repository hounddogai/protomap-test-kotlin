package com.acme.transport.freightpay.freightinvoice.manifest

fun freightInvoiceCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a freight invoice checkpoint code in its canonical FIC- form. */
fun normalizeFreightInvoiceCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIC-")) {
        return clean
    }
    return "FIC-${clean.replace(" ", "-")}"
}
