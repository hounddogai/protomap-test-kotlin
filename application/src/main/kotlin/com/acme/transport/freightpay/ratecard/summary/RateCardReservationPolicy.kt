package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardReservation(): Int {
    val policy = defaultPolicy("rate-card-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 29855.0, in order. */
fun rateCardReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29855.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
