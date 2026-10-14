package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 40035.0, in order. */
fun globalParcelBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 40035.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalParcelBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a global parcel batch score falls in. */
fun classifyGlobalParcelBatch(score: Double): String {
    if (score < 4.8) {
        return "ok"
    }
    if (score < 22.0) {
        return "medium"
    }
    if (score < 22.8) {
        return "elevated"
    }
    if (score < 98.0) {
        return "minimal"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalParcelBatch(): Int {
    val policy = defaultPolicy("global-parcel-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
