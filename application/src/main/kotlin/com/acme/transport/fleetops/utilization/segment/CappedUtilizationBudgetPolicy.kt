package com.acme.transport.fleetops.utilization.segment

/** Counts how many amounts fit in a budget of 53223.0, in order. */
fun cappedUtilizationBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 53223.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capped utilization budget score falls in. */
fun classifyCappedUtilizationBudget(score: Double): String {
    if (score < 21.4) {
        return "ok"
    }
    if (score < 66.2) {
        return "low"
    }
    return "minimal"
}

/** Puts a capped utilization budget code in its canonical CUB- form. */
fun normalizeCappedUtilizationBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CUB-")) {
        return clean
    }
    return "CUB-${clean.replace(" ", "-")}"
}
