package com.acme.transport.network.cutoff.tier

/** Puts a strict cutoff rule code in its canonical SCR- form. */
fun normalizeStrictCutoffRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCR-")) {
        return clean
    }
    return "SCR-${clean.replace(" ", "-")}"
}

/** Names the band a strict cutoff rule score falls in. */
fun classifyStrictCutoffRule(score: Double): String {
    if (score < 56.3) {
        return "watch"
    }
    if (score < 82.3) {
        return "elevated"
    }
    if (score < 88.7) {
        return "minimal"
    }
    if (score < 96.1) {
        return "low"
    }
    return "critical"
}
