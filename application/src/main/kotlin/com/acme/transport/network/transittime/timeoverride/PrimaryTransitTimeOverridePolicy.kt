package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.kit.numbers.roundTo

/** Names the band a primary transit time override score falls in. */
fun classifyPrimaryTransitTimeOverride(score: Double): String {
    if (score < 11.3) {
        return "high"
    }
    if (score < 23.0) {
        return "low"
    }
    if (score < 71.4) {
        return "watch"
    }
    if (score < 76.2) {
        return "ok"
    }
    return "minimal"
}

fun primaryTransitTimeOverrideBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a primary transit time override code in its canonical PTTO- form. */
fun normalizePrimaryTransitTimeOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTTO-")) {
        return clean
    }
    return "PTTO-${clean.replace(" ", "-")}"
}
