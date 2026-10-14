package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tiered parcel cycle score falls in. */
fun classifyTieredParcelCycle(score: Double): String {
    if (score < 49.0) {
        return "critical"
    }
    if (score < 70.6) {
        return "minimal"
    }
    if (score < 86.0) {
        return "watch"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 22898.0, in order. */
fun tieredParcelCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22898.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredParcelCycle(): Int {
    val policy = defaultPolicy("tiered-parcel-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
