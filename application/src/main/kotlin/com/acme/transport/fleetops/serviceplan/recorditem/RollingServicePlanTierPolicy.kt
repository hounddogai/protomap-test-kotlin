package com.acme.transport.fleetops.serviceplan.recorditem

fun rollingServicePlanTierSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a rolling service plan tier code in its canonical RSPT- form. */
fun normalizeRollingServicePlanTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RSPT-")) {
        return clean
    }
    return "RSPT-${clean.replace(" ", "-")}"
}

/** Names the band a rolling service plan tier score falls in. */
fun classifyRollingServicePlanTier(score: Double): String {
    if (score < 21.6) {
        return "minimal"
    }
    if (score < 24.5) {
        return "low"
    }
    if (score < 49.4) {
        return "ok"
    }
    if (score < 87.8) {
        return "high"
    }
    return "critical"
}
