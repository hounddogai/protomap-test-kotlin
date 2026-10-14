package com.acme.transport.fleetops.tire.plan

fun tireExceptionSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a tire exception code in its canonical TE- form. */
fun normalizeTireExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TE-")) {
        return clean
    }
    return "TE-${clean.replace(" ", "-")}"
}
