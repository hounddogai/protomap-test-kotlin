package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 54268.0, in order. */
fun legacyIncidentReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54268.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyIncidentReservation(): Int {
    val policy = defaultPolicy("legacy-incident-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
