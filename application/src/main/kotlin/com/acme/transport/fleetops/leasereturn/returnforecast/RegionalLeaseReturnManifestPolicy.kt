package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalLeaseReturnManifest(): Int {
    val policy = defaultPolicy("regional-lease-return-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional lease return manifest score falls in. */
fun classifyRegionalLeaseReturnManifest(score: Double): String {
    if (score < 5.4) {
        return "ok"
    }
    if (score < 35.9) {
        return "minimal"
    }
    if (score < 53.7) {
        return "critical"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 17497.0, in order. */
fun regionalLeaseReturnManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17497.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalLeaseReturnManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
