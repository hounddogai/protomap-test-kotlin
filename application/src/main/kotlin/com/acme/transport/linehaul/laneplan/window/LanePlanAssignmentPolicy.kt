package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanAssignment(): Int {
    val policy = defaultPolicy("lane-plan-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 65804.0, in order. */
fun lanePlanAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 65804.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lane plan assignment score falls in. */
fun classifyLanePlanAssignment(score: Double): String {
    if (score < 5.2) {
        return "watch"
    }
    if (score < 5.3) {
        return "critical"
    }
    return "medium"
}

fun lanePlanAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
