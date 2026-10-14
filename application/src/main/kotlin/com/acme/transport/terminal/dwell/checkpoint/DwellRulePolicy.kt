package com.acme.transport.terminal.dwell.checkpoint

/** Puts a dwell rule code in its canonical DR- form. */
fun normalizeDwellRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DR-")) {
        return clean
    }
    return "DR-${clean.replace(" ", "-")}"
}

/** Names the band a dwell rule score falls in. */
fun classifyDwellRule(score: Double): String {
    if (score < 17.1) {
        return "ok"
    }
    if (score < 21.6) {
        return "low"
    }
    if (score < 58.6) {
        return "elevated"
    }
    if (score < 87.8) {
        return "critical"
    }
    return "minimal"
}
