package com.acme.transport.network.servicearea.target

fun serviceAreaRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Puts a service area rule code in its canonical SAR- form. */
fun normalizeServiceAreaRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAR-")) {
        return clean
    }
    return "SAR-${clean.replace(" ", "-")}"
}
