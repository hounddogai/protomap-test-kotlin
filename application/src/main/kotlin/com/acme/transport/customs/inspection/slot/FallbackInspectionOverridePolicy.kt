package com.acme.transport.customs.inspection.slot

/** Puts a fallback inspection override code in its canonical FIO- form. */
fun normalizeFallbackInspectionOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FIO-")) {
        return clean
    }
    return "FIO-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 48976.0, in order. */
fun fallbackInspectionOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48976.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a fallback inspection override score falls in. */
fun classifyFallbackInspectionOverride(score: Double): String {
    if (score < 9.3) {
        return "critical"
    }
    if (score < 20.6) {
        return "ok"
    }
    if (score < 29.9) {
        return "watch"
    }
    if (score < 34.4) {
        return "low"
    }
    return "high"
}
