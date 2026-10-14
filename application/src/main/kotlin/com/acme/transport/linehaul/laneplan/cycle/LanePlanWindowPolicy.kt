package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.numbers.roundTo

fun lanePlanWindowBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a lane plan window code in its canonical LPW- form. */
fun normalizeLanePlanWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPW-")) {
        return clean
    }
    return "LPW-${clean.replace(" ", "-")}"
}

/** Names the band a lane plan window score falls in. */
fun classifyLanePlanWindow(score: Double): String {
    if (score < 25.6) {
        return "watch"
    }
    if (score < 34.4) {
        return "ok"
    }
    if (score < 36.4) {
        return "medium"
    }
    if (score < 74.5) {
        return "critical"
    }
    return "minimal"
}
