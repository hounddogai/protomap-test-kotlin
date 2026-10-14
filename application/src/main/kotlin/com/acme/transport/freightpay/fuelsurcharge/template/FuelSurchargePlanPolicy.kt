package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelSurchargePlan(): Int {
    val policy = defaultPolicy("fuel-surcharge-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 691.0, in order. */
fun fuelSurchargePlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 691.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelSurchargePlanBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
