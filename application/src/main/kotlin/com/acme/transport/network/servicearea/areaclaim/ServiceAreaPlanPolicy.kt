package com.acme.transport.network.servicearea.areaclaim

/** Puts a service area plan code in its canonical SAP- form. */
fun normalizeServiceAreaPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAP-")) {
        return clean
    }
    return "SAP-${clean.replace(" ", "-")}"
}

/** Names the band a service area plan score falls in. */
fun classifyServiceAreaPlan(score: Double): String {
    if (score < 3.5) {
        return "low"
    }
    if (score < 18.2) {
        return "minimal"
    }
    return "critical"
}
