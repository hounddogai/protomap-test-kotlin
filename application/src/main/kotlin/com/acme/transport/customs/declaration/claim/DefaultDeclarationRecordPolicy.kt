package com.acme.transport.customs.declaration.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultDeclarationRecord(): Int {
    val policy = defaultPolicy("default-declaration-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 66239.0, in order. */
fun defaultDeclarationRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66239.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
