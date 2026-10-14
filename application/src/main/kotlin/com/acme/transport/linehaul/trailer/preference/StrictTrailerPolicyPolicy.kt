package com.acme.transport.linehaul.trailer.preference

fun strictTrailerPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a strict trailer policy code in its canonical STP- form. */
fun normalizeStrictTrailerPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STP-")) {
        return clean
    }
    return "STP-${clean.replace(" ", "-")}"
}
