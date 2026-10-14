package com.acme.transport.fleetops.utilization.assignment

fun weightedUtilizationRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a weighted utilization rule score falls in. */
fun classifyWeightedUtilizationRule(score: Double): String {
    if (score < 13.8) {
        return "ok"
    }
    if (score < 50.7) {
        return "low"
    }
    if (score < 71.3) {
        return "medium"
    }
    return "critical"
}

/** Puts a weighted utilization rule code in its canonical WUR- form. */
fun normalizeWeightedUtilizationRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WUR-")) {
        return clean
    }
    return "WUR-${clean.replace(" ", "-")}"
}
