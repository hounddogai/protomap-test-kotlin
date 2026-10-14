package com.acme.transport.lastmile.drop.batcheddropnotice

fun dropTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a drop template code in its canonical DT- form. */
fun normalizeDropTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DT-")) {
        return clean
    }
    return "DT-${clean.replace(" ", "-")}"
}
