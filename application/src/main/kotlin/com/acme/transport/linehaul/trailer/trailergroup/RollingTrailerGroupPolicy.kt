package com.acme.transport.linehaul.trailer.trailergroup

/** Puts a rolling trailer group code in its canonical RTG- form. */
fun normalizeRollingTrailerGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTG-")) {
        return clean
    }
    return "RTG-${clean.replace(" ", "-")}"
}

fun rollingTrailerGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
