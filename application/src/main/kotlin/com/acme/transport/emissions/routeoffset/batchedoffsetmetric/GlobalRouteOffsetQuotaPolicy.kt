package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalRouteOffsetQuota(): Int {
    val policy = defaultPolicy("global-route-offset-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 88655.0, in order. */
fun globalRouteOffsetQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88655.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalRouteOffsetQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalRouteOffsetQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
