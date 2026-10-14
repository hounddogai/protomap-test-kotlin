package com.acme.transport.freightpay.freightinvoice.notice

fun lenientFreightInvoiceTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a lenient freight invoice target code in its canonical LFIT- form. */
fun normalizeLenientFreightInvoiceTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LFIT-")) {
        return clean
    }
    return "LFIT-${clean.replace(" ", "-")}"
}
