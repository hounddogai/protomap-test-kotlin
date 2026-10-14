package com.acme.transport.emissions.fuelburn.baseline

fun manualFuelBurnVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a manual fuel burn variance score falls in. */
fun classifyManualFuelBurnVariance(score: Double): String {
    if (score < 47.6) {
        return "minimal"
    }
    if (score < 53.1) {
        return "low"
    }
    if (score < 71.3) {
        return "high"
    }
    return "ok"
}

/** Puts a manual fuel burn variance code in its canonical MFBV- form. */
fun normalizeManualFuelBurnVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MFBV-")) {
        return clean
    }
    return "MFBV-${clean.replace(" ", "-")}"
}
