package com.acme.transport.network.zone.zonemapping

/** Counts how many amounts fit in a budget of 9509.0, in order. */
fun batchedZoneAgreementBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9509.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a batched zone agreement score falls in. */
fun classifyBatchedZoneAgreement(score: Double): String {
    if (score < 13.4) {
        return "low"
    }
    if (score < 30.3) {
        return "medium"
    }
    if (score < 78.2) {
        return "minimal"
    }
    return "high"
}
