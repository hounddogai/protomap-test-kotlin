package com.acme.transport.terminal.dockdoor.claim

/** Names the band a pending dock door assignment score falls in. */
fun classifyPendingDockDoorAssignment(score: Double): String {
    if (score < 24.1) {
        return "high"
    }
    if (score < 64.9) {
        return "low"
    }
    return "elevated"
}

/** Puts a pending dock door assignment code in its canonical PDDA- form. */
fun normalizePendingDockDoorAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDDA-")) {
        return clean
    }
    return "PDDA-${clean.replace(" ", "-")}"
}

fun pendingDockDoorAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
