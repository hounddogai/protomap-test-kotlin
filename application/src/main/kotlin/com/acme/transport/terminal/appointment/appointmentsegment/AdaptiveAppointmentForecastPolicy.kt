package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveAppointmentForecast(): Int {
    val policy = defaultPolicy("adaptive-appointment-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 91866.0, in order. */
fun adaptiveAppointmentForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91866.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
