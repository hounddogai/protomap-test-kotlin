package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.numbers.roundTo

/** Names the band a trailer preference score falls in. */
fun classifyTrailerPreference(score: Double): String {
    if (score < 17.0) {
        return "elevated"
    }
    if (score < 26.7) {
        return "minimal"
    }
    return "medium"
}

/** Puts a trailer preference code in its canonical TP- form. */
fun normalizeTrailerPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TP-")) {
        return clean
    }
    return "TP-${clean.replace(" ", "-")}"
}

fun trailerPreferenceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
