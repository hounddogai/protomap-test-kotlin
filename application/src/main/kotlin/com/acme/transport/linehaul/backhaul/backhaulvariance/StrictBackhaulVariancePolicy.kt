package com.acme.transport.linehaul.backhaul.backhaulvariance

/** Puts a strict backhaul variance code in its canonical SBV- form. */
fun normalizeStrictBackhaulVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SBV-")) {
        return clean
    }
    return "SBV-${clean.replace(" ", "-")}"
}

fun strictBackhaulVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
