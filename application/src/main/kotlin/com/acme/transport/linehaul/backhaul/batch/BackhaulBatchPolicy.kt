package com.acme.transport.linehaul.backhaul.batch

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBackhaulBatch(): Int {
    val policy = defaultPolicy("backhaul-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 66504.0, in order. */
fun backhaulBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66504.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
