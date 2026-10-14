package com.acme.transport.emissions.fuelburn.request

fun primaryFuelBurnRequestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a primary fuel burn request code in its canonical PFBR- form. */
fun normalizePrimaryFuelBurnRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFBR-")) {
        return clean
    }
    return "PFBR-${clean.replace(" ", "-")}"
}

/** Names the band a primary fuel burn request score falls in. */
fun classifyPrimaryFuelBurnRequest(score: Double): String {
    if (score < 22.3) {
        return "high"
    }
    if (score < 58.2) {
        return "minimal"
    }
    if (score < 88.4) {
        return "ok"
    }
    return "elevated"
}
