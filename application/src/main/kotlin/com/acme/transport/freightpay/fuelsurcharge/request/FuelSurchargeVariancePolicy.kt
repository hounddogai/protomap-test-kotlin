package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 92981.0, in order. */
fun fuelSurchargeVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92981.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelSurchargeVarianceBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelSurchargeVariance(): Int {
    val policy = defaultPolicy("fuel-surcharge-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fuel surcharge variance score falls in. */
fun classifyFuelSurchargeVariance(score: Double): String {
    if (score < 77.7) {
        return "watch"
    }
    if (score < 79.8) {
        return "elevated"
    }
    return "high"
}
