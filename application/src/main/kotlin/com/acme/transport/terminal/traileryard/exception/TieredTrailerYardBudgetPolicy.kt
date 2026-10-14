package com.acme.transport.terminal.traileryard.exception

/** Names the band a tiered trailer yard budget score falls in. */
fun classifyTieredTrailerYardBudget(score: Double): String {
    if (score < 17.3) {
        return "medium"
    }
    if (score < 53.8) {
        return "low"
    }
    if (score < 67.2) {
        return "watch"
    }
    if (score < 79.7) {
        return "elevated"
    }
    return "critical"
}

/** Puts a tiered trailer yard budget code in its canonical TTYB- form. */
fun normalizeTieredTrailerYardBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTYB-")) {
        return clean
    }
    return "TTYB-${clean.replace(" ", "-")}"
}
