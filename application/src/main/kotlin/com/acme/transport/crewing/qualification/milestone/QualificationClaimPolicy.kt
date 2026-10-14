package com.acme.transport.crewing.qualification.milestone

fun qualificationClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a qualification claim code in its canonical QC- form. */
fun normalizeQualificationClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QC-")) {
        return clean
    }
    return "QC-${clean.replace(" ", "-")}"
}
