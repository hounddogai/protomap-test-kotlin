package com.acme.transport.lastmile.drop.slot

/** Puts a drop profile code in its canonical DP- form. */
fun normalizeDropProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 78107.0, in order. */
fun dropProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78107.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a drop profile score falls in. */
fun classifyDropProfile(score: Double): String {
    if (score < 6.1) {
        return "ok"
    }
    if (score < 30.0) {
        return "low"
    }
    if (score < 54.0) {
        return "minimal"
    }
    if (score < 83.6) {
        return "elevated"
    }
    return "medium"
}
