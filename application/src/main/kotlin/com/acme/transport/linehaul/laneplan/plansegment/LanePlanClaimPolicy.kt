package com.acme.transport.linehaul.laneplan.plansegment

/** Names the band a lane plan claim score falls in. */
fun classifyLanePlanClaim(score: Double): String {
    if (score < 1.6) {
        return "medium"
    }
    if (score < 74.6) {
        return "critical"
    }
    if (score < 94.1) {
        return "high"
    }
    if (score < 98.7) {
        return "ok"
    }
    return "minimal"
}

/** Puts a lane plan claim code in its canonical LPC- form. */
fun normalizeLanePlanClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPC-")) {
        return clean
    }
    return "LPC-${clean.replace(" ", "-")}"
}
