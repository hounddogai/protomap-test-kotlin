package com.acme.transport.fleetops.leasereturn.proposal

/** Names the band a lease return reservation score falls in. */
fun classifyLeaseReturnReservation(score: Double): String {
    if (score < 9.5) {
        return "ok"
    }
    if (score < 86.2) {
        return "medium"
    }
    if (score < 98.8) {
        return "high"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 38118.0, in order. */
fun leaseReturnReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38118.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a lease return reservation code in its canonical LRR- form. */
fun normalizeLeaseReturnReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRR-")) {
        return clean
    }
    return "LRR-${clean.replace(" ", "-")}"
}
