package com.acme.transport.crewing.restbreak.breakmapping

/** Puts a pending rest break mapping code in its canonical PRBM- form. */
fun normalizePendingRestBreakMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRBM-")) {
        return clean
    }
    return "PRBM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 35100.0, in order. */
fun pendingRestBreakMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35100.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a pending rest break mapping score falls in. */
fun classifyPendingRestBreakMapping(score: Double): String {
    if (score < 40.4) {
        return "critical"
    }
    if (score < 90.4) {
        return "minimal"
    }
    return "high"
}
