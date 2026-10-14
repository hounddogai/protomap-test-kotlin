package com.acme.transport.fleetops.utilization.group

/** Puts a tiered utilization group code in its canonical TUG- form. */
fun normalizeTieredUtilizationGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TUG-")) {
        return clean
    }
    return "TUG-${clean.replace(" ", "-")}"
}

/** Names the band a tiered utilization group score falls in. */
fun classifyTieredUtilizationGroup(score: Double): String {
    if (score < 6.3) {
        return "critical"
    }
    if (score < 66.8) {
        return "medium"
    }
    return "minimal"
}
