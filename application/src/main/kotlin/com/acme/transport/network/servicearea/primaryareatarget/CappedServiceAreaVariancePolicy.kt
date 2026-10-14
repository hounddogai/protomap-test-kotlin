package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 11533.0, in order. */
fun cappedServiceAreaVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11533.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedServiceAreaVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedServiceAreaVariance(): Int {
    val policy = defaultPolicy("capped-service-area-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
