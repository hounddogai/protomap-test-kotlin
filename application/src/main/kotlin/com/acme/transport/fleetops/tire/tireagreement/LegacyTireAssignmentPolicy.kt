package com.acme.transport.fleetops.tire.tireagreement

/** Names the band a legacy tire assignment score falls in. */
fun classifyLegacyTireAssignment(score: Double): String {
    if (score < 1.1) {
        return "medium"
    }
    if (score < 90.1) {
        return "elevated"
    }
    return "low"
}

/** Puts a legacy tire assignment code in its canonical LTA- form. */
fun normalizeLegacyTireAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTA-")) {
        return clean
    }
    return "LTA-${clean.replace(" ", "-")}"
}
