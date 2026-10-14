package com.acme.transport.lastmile.drop.notice

fun dropNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Puts a drop notice code in its canonical DN- form. */
fun normalizeDropNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DN-")) {
        return clean
    }
    return "DN-${clean.replace(" ", "-")}"
}
