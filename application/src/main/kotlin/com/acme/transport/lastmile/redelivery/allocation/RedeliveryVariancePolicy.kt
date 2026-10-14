package com.acme.transport.lastmile.redelivery.allocation

fun redeliveryVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a redelivery variance code in its canonical RV- form. */
fun normalizeRedeliveryVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RV-")) {
        return clean
    }
    return "RV-${clean.replace(" ", "-")}"
}
