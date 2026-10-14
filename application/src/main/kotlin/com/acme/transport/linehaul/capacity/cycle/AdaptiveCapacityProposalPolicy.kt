package com.acme.transport.linehaul.capacity.cycle

fun adaptiveCapacityProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a adaptive capacity proposal code in its canonical ACP- form. */
fun normalizeAdaptiveCapacityProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACP-")) {
        return clean
    }
    return "ACP-${clean.replace(" ", "-")}"
}
