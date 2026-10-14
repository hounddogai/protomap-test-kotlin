package com.acme.transport.crewing.dutyperiod.periodthreshold

/** Names the band a adaptive duty period bundle score falls in. */
fun classifyAdaptiveDutyPeriodBundle(score: Double): String {
    if (score < 6.6) {
        return "low"
    }
    if (score < 32.4) {
        return "high"
    }
    if (score < 53.2) {
        return "medium"
    }
    return "elevated"
}

/** Puts a adaptive duty period bundle code in its canonical ADPB- form. */
fun normalizeAdaptiveDutyPeriodBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADPB-")) {
        return clean
    }
    return "ADPB-${clean.replace(" ", "-")}"
}
