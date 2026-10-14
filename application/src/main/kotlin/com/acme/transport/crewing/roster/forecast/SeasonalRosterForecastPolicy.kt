package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a seasonal roster forecast code in its canonical SRF- form. */
fun normalizeSeasonalRosterForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRF-")) {
        return clean
    }
    return "SRF-${clean.replace(" ", "-")}"
}

fun mergeSeasonalRosterForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a seasonal roster forecast score falls in. */
fun classifySeasonalRosterForecast(score: Double): String {
    if (score < 2.3) {
        return "low"
    }
    if (score < 42.7) {
        return "elevated"
    }
    if (score < 49.5) {
        return "minimal"
    }
    return "high"
}
