package com.acme.transport.network.servicearea.budget

fun serviceAreaOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a service area override code in its canonical SAO- form. */
fun normalizeServiceAreaOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAO-")) {
        return clean
    }
    return "SAO-${clean.replace(" ", "-")}"
}
