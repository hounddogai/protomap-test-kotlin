package com.acme.transport.linehaul.laneplan.item

/** Puts a lane plan item code in its canonical LPI- form. */
fun normalizeLanePlanItemCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPI-")) {
        return clean
    }
    return "LPI-${clean.replace(" ", "-")}"
}

/** Names the band a lane plan item score falls in. */
fun classifyLanePlanItem(score: Double): String {
    if (score < 16.0) {
        return "high"
    }
    if (score < 47.9) {
        return "medium"
    }
    if (score < 87.9) {
        return "minimal"
    }
    if (score < 96.0) {
        return "elevated"
    }
    return "critical"
}
