package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelSurchargePolicy(): Int {
    val policy = defaultPolicy("fuel-surcharge-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 25686.0, in order. */
fun fuelSurchargePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25686.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
