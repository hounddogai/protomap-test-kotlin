package com.acme.transport.linehaul.tender.profile

/** Puts a tender exception code in its canonical TE- form. */
fun normalizeTenderExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TE-")) {
        return clean
    }
    return "TE-${clean.replace(" ", "-")}"
}

fun tenderExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
