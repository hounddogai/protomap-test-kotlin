package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedServicePlanManifest(): Int {
    val policy = defaultPolicy("weighted-service-plan-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 78176.0, in order. */
fun weightedServicePlanManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78176.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
