package com.acme.transport.fleetops.leasereturn.returnledger

/** Names the band a legacy lease return reservation score falls in. */
fun classifyLegacyLeaseReturnReservation(score: Double): String {
    if (score < 6.8) {
        return "ok"
    }
    if (score < 75.3) {
        return "elevated"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 40887.0, in order. */
fun legacyLeaseReturnReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40887.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
