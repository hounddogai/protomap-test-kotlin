package com.acme.transport.network.zone.reservation

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingZoneReservation(): Int {
    val policy = defaultPolicy("pending-zone-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 52357.0, in order. */
fun pendingZoneReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 52357.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
