package com.acme.transport.fleetops.serviceplan.variance

/** Puts a pending service plan ledger code in its canonical PSPL- form. */
fun normalizePendingServicePlanLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSPL-")) {
        return clean
    }
    return "PSPL-${clean.replace(" ", "-")}"
}

/** Names the band a pending service plan ledger score falls in. */
fun classifyPendingServicePlanLedger(score: Double): String {
    if (score < 12.0) {
        return "low"
    }
    if (score < 71.5) {
        return "minimal"
    }
    if (score < 72.9) {
        return "medium"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 46668.0, in order. */
fun pendingServicePlanLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46668.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
