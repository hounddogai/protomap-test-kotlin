package com.acme.transport.terminal.sortation.estimate

fun lenientSortationTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a lenient sortation template code in its canonical LST- form. */
fun normalizeLenientSortationTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LST-")) {
        return clean
    }
    return "LST-${clean.replace(" ", "-")}"
}
