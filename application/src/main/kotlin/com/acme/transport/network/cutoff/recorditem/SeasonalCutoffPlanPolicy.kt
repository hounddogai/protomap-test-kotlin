package com.acme.transport.network.cutoff.recorditem

/** Puts a seasonal cutoff plan code in its canonical SCP- form. */
fun normalizeSeasonalCutoffPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCP-")) {
        return clean
    }
    return "SCP-${clean.replace(" ", "-")}"
}

fun seasonalCutoffPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a seasonal cutoff plan score falls in. */
fun classifySeasonalCutoffPlan(score: Double): String {
    if (score < 32.1) {
        return "elevated"
    }
    if (score < 58.3) {
        return "critical"
    }
    if (score < 71.3) {
        return "minimal"
    }
    return "ok"
}
