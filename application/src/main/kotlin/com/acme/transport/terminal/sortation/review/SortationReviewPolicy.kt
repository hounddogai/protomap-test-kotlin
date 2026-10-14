package com.acme.transport.terminal.sortation.review

/** Puts a sortation review code in its canonical SR- form. */
fun normalizeSortationReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SR-")) {
        return clean
    }
    return "SR-${clean.replace(" ", "-")}"
}

fun sortationReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
