package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 71539.0, in order. */
fun carbonReportReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71539.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCarbonReportReservation(): Int {
    val policy = defaultPolicy("carbon-report-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
