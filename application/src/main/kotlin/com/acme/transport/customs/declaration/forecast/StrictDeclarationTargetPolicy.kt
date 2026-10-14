package com.acme.transport.customs.declaration.forecast

/** Puts a strict declaration target code in its canonical SDT- form. */
fun normalizeStrictDeclarationTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDT-")) {
        return clean
    }
    return "SDT-${clean.replace(" ", "-")}"
}

fun strictDeclarationTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
