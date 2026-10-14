package com.acme.transport.lastmile.attempt.attemptplan

/** Puts a attempt assignment code in its canonical AA- form. */
fun normalizeAttemptAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AA-")) {
        return clean
    }
    return "AA-${clean.replace(" ", "-")}"
}

/** Names the band a attempt assignment score falls in. */
fun classifyAttemptAssignment(score: Double): String {
    if (score < 27.4) {
        return "medium"
    }
    if (score < 30.1) {
        return "critical"
    }
    if (score < 85.4) {
        return "high"
    }
    if (score < 87.1) {
        return "minimal"
    }
    return "low"
}
