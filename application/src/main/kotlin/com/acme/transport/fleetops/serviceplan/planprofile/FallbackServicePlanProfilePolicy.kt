package com.acme.transport.fleetops.serviceplan.planprofile

/** Puts a fallback service plan profile code in its canonical FSPP- form. */
fun normalizeFallbackServicePlanProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSPP-")) {
        return clean
    }
    return "FSPP-${clean.replace(" ", "-")}"
}

/** Names the band a fallback service plan profile score falls in. */
fun classifyFallbackServicePlanProfile(score: Double): String {
    if (score < 2.4) {
        return "ok"
    }
    if (score < 22.3) {
        return "medium"
    }
    if (score < 80.8) {
        return "watch"
    }
    return "low"
}
