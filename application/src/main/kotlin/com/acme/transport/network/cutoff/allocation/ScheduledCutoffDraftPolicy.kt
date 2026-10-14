package com.acme.transport.network.cutoff.allocation

/** Names the band a scheduled cutoff draft score falls in. */
fun classifyScheduledCutoffDraft(score: Double): String {
    if (score < 16.0) {
        return "ok"
    }
    if (score < 56.9) {
        return "elevated"
    }
    if (score < 70.6) {
        return "high"
    }
    if (score < 76.6) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 29459.0, in order. */
fun scheduledCutoffDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29459.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled cutoff draft code in its canonical SCD- form. */
fun normalizeScheduledCutoffDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCD-")) {
        return clean
    }
    return "SCD-${clean.replace(" ", "-")}"
}
