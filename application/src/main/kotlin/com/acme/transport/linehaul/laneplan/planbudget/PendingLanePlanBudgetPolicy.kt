package com.acme.transport.linehaul.laneplan.planbudget

/** Puts a pending lane plan budget code in its canonical PLPB- form. */
fun normalizePendingLanePlanBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLPB-")) {
        return clean
    }
    return "PLPB-${clean.replace(" ", "-")}"
}

fun pendingLanePlanBudgetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a pending lane plan budget score falls in. */
fun classifyPendingLanePlanBudget(score: Double): String {
    if (score < 35.6) {
        return "minimal"
    }
    if (score < 40.2) {
        return "critical"
    }
    if (score < 47.6) {
        return "watch"
    }
    if (score < 57.4) {
        return "ok"
    }
    return "low"
}
