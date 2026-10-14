package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingDockDoorThreshold(): Int {
    val policy = defaultPolicy("rolling-dock-door-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingDockDoorThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 82006.0, in order. */
fun rollingDockDoorThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82006.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
