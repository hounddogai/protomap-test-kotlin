package com.acme.transport.customs.declaration.window

/** Names the band a pending declaration forecast score falls in. */
fun classifyPendingDeclarationForecast(score: Double): String {
    if (score < 1.8) {
        return "medium"
    }
    if (score < 17.9) {
        return "critical"
    }
    if (score < 45.4) {
        return "low"
    }
    if (score < 89.4) {
        return "high"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 34956.0, in order. */
fun pendingDeclarationForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34956.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingDeclarationForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a pending declaration forecast code in its canonical PDF- form. */
fun normalizePendingDeclarationForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDF-")) {
        return clean
    }
    return "PDF-${clean.replace(" ", "-")}"
}
