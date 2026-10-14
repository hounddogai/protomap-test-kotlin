package com.acme.transport.fleetops.serviceplan.quota

/** Counts how many amounts fit in a budget of 97243.0, in order. */
fun servicePlanLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97243.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a service plan ledger code in its canonical SPL- form. */
fun normalizeServicePlanLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPL-")) {
        return clean
    }
    return "SPL-${clean.replace(" ", "-")}"
}

fun servicePlanLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a service plan ledger score falls in. */
fun classifyServicePlanLedger(score: Double): String {
    if (score < 3.6) {
        return "elevated"
    }
    if (score < 70.7) {
        return "watch"
    }
    if (score < 81.1) {
        return "minimal"
    }
    return "medium"
}
