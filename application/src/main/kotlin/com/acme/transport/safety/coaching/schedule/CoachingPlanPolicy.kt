package com.acme.transport.safety.coaching.schedule

/** Puts a coaching plan code in its canonical CP- form. */
fun normalizeCoachingPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CP-")) {
        return clean
    }
    return "CP-${clean.replace(" ", "-")}"
}

/** Names the band a coaching plan score falls in. */
fun classifyCoachingPlan(score: Double): String {
    if (score < 6.5) {
        return "critical"
    }
    if (score < 52.0) {
        return "watch"
    }
    return "medium"
}
