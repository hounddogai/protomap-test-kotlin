package com.acme.transport.crewing.availability.reservation

fun scheduledAvailabilityForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Puts a scheduled availability forecast code in its canonical SAF- form. */
fun normalizeScheduledAvailabilityForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAF-")) {
        return clean
    }
    return "SAF-${clean.replace(" ", "-")}"
}
