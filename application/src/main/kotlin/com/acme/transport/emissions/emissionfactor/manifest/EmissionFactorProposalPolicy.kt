package com.acme.transport.emissions.emissionfactor.manifest

fun emissionFactorProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a emission factor proposal code in its canonical EFP- form. */
fun normalizeEmissionFactorProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFP-")) {
        return clean
    }
    return "EFP-${clean.replace(" ", "-")}"
}
