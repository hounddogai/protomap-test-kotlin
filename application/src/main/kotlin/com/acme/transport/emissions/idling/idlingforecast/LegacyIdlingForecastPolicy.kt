package com.acme.transport.emissions.idling.idlingforecast

/** Puts a legacy idling forecast code in its canonical LIF- form. */
fun normalizeLegacyIdlingForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LIF-")) {
        return clean
    }
    return "LIF-${clean.replace(" ", "-")}"
}

/** Names the band a legacy idling forecast score falls in. */
fun classifyLegacyIdlingForecast(score: Double): String {
    if (score < 73.5) {
        return "watch"
    }
    if (score < 88.7) {
        return "minimal"
    }
    return "critical"
}
