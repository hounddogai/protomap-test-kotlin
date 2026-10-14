package com.acme.transport.lastmile.proofofdelivery.estimate

/** Puts a adaptive proof of delivery schedule code in its canonical APODS- form. */
fun normalizeAdaptiveProofOfDeliveryScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("APODS-")) {
        return clean
    }
    return "APODS-${clean.replace(" ", "-")}"
}

/** Names the band a adaptive proof of delivery schedule score falls in. */
fun classifyAdaptiveProofOfDeliverySchedule(score: Double): String {
    if (score < 79.4) {
        return "critical"
    }
    if (score < 79.9) {
        return "elevated"
    }
    return "ok"
}
