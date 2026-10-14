package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.retry.defaultPolicy

fun appointmentCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAppointmentCheckpoint(): Int {
    val policy = defaultPolicy("appointment-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 4941.0, in order. */
fun appointmentCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 4941.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
