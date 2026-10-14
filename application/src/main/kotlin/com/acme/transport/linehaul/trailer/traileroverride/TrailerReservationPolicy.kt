package com.acme.transport.linehaul.trailer.traileroverride

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 25800.0, in order. */
fun trailerReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25800.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerReservation(): Int {
    val policy = defaultPolicy("trailer-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
