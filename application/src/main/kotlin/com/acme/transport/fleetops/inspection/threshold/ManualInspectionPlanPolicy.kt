package com.acme.transport.fleetops.inspection.threshold

/** Puts a manual inspection plan code in its canonical MIP- form. */
fun normalizeManualInspectionPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MIP-")) {
        return clean
    }
    return "MIP-${clean.replace(" ", "-")}"
}

/** Names the band a manual inspection plan score falls in. */
fun classifyManualInspectionPlan(score: Double): String {
    if (score < 10.5) {
        return "ok"
    }
    if (score < 54.9) {
        return "critical"
    }
    if (score < 60.4) {
        return "medium"
    }
    return "minimal"
}
