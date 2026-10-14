package com.acme.transport.fleetops.leasereturn.returnentry

/** Names the band a scheduled lease return estimate score falls in. */
fun classifyScheduledLeaseReturnEstimate(score: Double): String {
    if (score < 34.3) {
        return "medium"
    }
    if (score < 74.9) {
        return "ok"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 34314.0, in order. */
fun scheduledLeaseReturnEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34314.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled lease return estimate code in its canonical SLRE- form. */
fun normalizeScheduledLeaseReturnEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SLRE-")) {
        return clean
    }
    return "SLRE-${clean.replace(" ", "-")}"
}
