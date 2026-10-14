package com.acme.transport.freightpay.audit.proposal

/** Puts a audit proposal code in its canonical AP- form. */
fun normalizeAuditProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AP-")) {
        return clean
    }
    return "AP-${clean.replace(" ", "-")}"
}

fun auditProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
