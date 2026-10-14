package com.acme.transport.terminal.dockdoor.assignment

/** Counts how many amounts fit in a budget of 97449.0, in order. */
fun defaultDockDoorSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97449.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a default dock door segment score falls in. */
fun classifyDefaultDockDoorSegment(score: Double): String {
    if (score < 19.6) {
        return "critical"
    }
    if (score < 35.0) {
        return "ok"
    }
    if (score < 50.2) {
        return "minimal"
    }
    if (score < 65.3) {
        return "low"
    }
    return "medium"
}

/** Puts a default dock door segment code in its canonical DDDS- form. */
fun normalizeDefaultDockDoorSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDDS-")) {
        return clean
    }
    return "DDDS-${clean.replace(" ", "-")}"
}
