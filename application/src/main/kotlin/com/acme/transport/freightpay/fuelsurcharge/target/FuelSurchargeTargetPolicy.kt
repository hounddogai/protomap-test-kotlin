package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.retry.defaultPolicy

fun fuelSurchargeTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Counts how many amounts fit in a budget of 39045.0, in order. */
fun fuelSurchargeTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39045.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelSurchargeTarget(): Int {
    val policy = defaultPolicy("fuel-surcharge-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fuel surcharge target score falls in. */
fun classifyFuelSurchargeTarget(score: Double): String {
    if (score < 46.8) {
        return "watch"
    }
    if (score < 72.8) {
        return "critical"
    }
    if (score < 73.3) {
        return "ok"
    }
    if (score < 95.0) {
        return "elevated"
    }
    return "minimal"
}
