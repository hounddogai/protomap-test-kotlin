package com.acme.transport.linehaul.backhaul.assignment

/** Names the band a backhaul assignment score falls in. */
fun classifyBackhaulAssignment(score: Double): String {
    if (score < 15.8) {
        return "medium"
    }
    if (score < 82.2) {
        return "low"
    }
    return "elevated"
}

/** Puts a backhaul assignment code in its canonical BA- form. */
fun normalizeBackhaulAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BA-")) {
        return clean
    }
    return "BA-${clean.replace(" ", "-")}"
}
