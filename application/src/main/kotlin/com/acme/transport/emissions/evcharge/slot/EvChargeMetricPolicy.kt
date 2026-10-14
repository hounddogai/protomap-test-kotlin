package com.acme.transport.emissions.evcharge.slot

fun evChargeMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a ev charge metric code in its canonical ECM- form. */
fun normalizeEvChargeMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECM-")) {
        return clean
    }
    return "ECM-${clean.replace(" ", "-")}"
}
