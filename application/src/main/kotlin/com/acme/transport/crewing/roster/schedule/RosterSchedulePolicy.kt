package com.acme.transport.crewing.roster.schedule

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 46917.0, in order. */
fun rosterScheduleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46917.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRosterSchedule(): Int {
    val policy = defaultPolicy("roster-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
