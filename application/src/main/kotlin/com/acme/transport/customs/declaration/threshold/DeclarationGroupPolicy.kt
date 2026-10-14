package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationGroup(): Int {
    val policy = defaultPolicy("declaration-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 74103.0, in order. */
fun declarationGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74103.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
