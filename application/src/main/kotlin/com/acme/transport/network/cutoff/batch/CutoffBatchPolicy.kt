package com.acme.transport.network.cutoff.batch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a cutoff batch score falls in. */
fun classifyCutoffBatch(score: Double): String {
    if (score < 1.3) {
        return "elevated"
    }
    if (score < 16.8) {
        return "low"
    }
    if (score < 62.6) {
        return "high"
    }
    return "critical"
}

fun cutoffBatchBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCutoffBatch(): Int {
    val policy = defaultPolicy("cutoff-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 93777.0, in order. */
fun cutoffBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93777.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
