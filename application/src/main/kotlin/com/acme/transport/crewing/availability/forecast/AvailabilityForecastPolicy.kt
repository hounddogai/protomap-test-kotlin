package com.acme.transport.crewing.availability.forecast

/** Puts a availability forecast code in its canonical AF- form. */
fun normalizeAvailabilityForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AF-")) {
        return clean
    }
    return "AF-${clean.replace(" ", "-")}"
}

/** Names the band a availability forecast score falls in. */
fun classifyAvailabilityForecast(score: Double): String {
    if (score < 18.1) {
        return "low"
    }
    if (score < 37.6) {
        return "high"
    }
    return "minimal"
}
