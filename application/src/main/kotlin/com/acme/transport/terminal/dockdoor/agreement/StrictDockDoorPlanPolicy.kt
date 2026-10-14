package com.acme.transport.terminal.dockdoor.agreement

/** Names the band a strict dock door plan score falls in. */
fun classifyStrictDockDoorPlan(score: Double): String {
    if (score < 12.8) {
        return "ok"
    }
    if (score < 32.7) {
        return "low"
    }
    if (score < 34.1) {
        return "minimal"
    }
    return "critical"
}

fun strictDockDoorPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a strict dock door plan code in its canonical SDDP- form. */
fun normalizeStrictDockDoorPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDDP-")) {
        return clean
    }
    return "SDDP-${clean.replace(" ", "-")}"
}
