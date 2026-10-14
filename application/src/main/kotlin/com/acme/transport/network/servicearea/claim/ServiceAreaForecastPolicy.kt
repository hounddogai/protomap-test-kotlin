package com.acme.transport.network.servicearea.claim

fun serviceAreaForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a service area forecast code in its canonical SAF- form. */
fun normalizeServiceAreaForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAF-")) {
        return clean
    }
    return "SAF-${clean.replace(" ", "-")}"
}
