package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictServicePlanAssignment(): Int {
    val policy = defaultPolicy("strict-service-plan-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun strictServicePlanAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 28680.0, in order. */
fun strictServicePlanAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28680.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
