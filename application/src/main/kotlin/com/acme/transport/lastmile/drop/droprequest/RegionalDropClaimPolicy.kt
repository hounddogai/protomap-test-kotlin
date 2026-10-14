package com.acme.transport.lastmile.drop.droprequest

/** Puts a regional drop claim code in its canonical RDC- form. */
fun normalizeRegionalDropClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDC-")) {
        return clean
    }
    return "RDC-${clean.replace(" ", "-")}"
}

/** Names the band a regional drop claim score falls in. */
fun classifyRegionalDropClaim(score: Double): String {
    if (score < 15.9) {
        return "low"
    }
    if (score < 86.4) {
        return "elevated"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 62128.0, in order. */
fun regionalDropClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62128.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
