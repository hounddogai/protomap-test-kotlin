package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 2510.0, in order. */
fun tariffAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2510.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTariffAssignment(): Int {
    val policy = defaultPolicy("tariff-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a tariff assignment score falls in. */
fun classifyTariffAssignment(score: Double): String {
    if (score < 19.6) {
        return "minimal"
    }
    if (score < 51.3) {
        return "elevated"
    }
    if (score < 81.2) {
        return "ok"
    }
    return "medium"
}
