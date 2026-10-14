package com.acme.transport.fleetops.utilization.score

/** Names the band a utilization tier score falls in. */
fun classifyUtilizationTier(score: Double): String {
    if (score < 44.3) {
        return "critical"
    }
    if (score < 97.3) {
        return "elevated"
    }
    return "ok"
}

/** Puts a utilization tier code in its canonical UT- form. */
fun normalizeUtilizationTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("UT-")) {
        return clean
    }
    return "UT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 12303.0, in order. */
fun utilizationTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12303.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
