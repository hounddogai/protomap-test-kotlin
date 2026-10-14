package com.acme.transport.emissions.routeoffset.offsetestimate

import com.acme.transport.kit.numbers.roundTo

/** Puts a pending route offset profile code in its canonical PROP- form. */
fun normalizePendingRouteOffsetProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PROP-")) {
        return clean
    }
    return "PROP-${clean.replace(" ", "-")}"
}

fun pendingRouteOffsetProfileBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a pending route offset profile score falls in. */
fun classifyPendingRouteOffsetProfile(score: Double): String {
    if (score < 4.4) {
        return "elevated"
    }
    if (score < 30.3) {
        return "medium"
    }
    return "low"
}
