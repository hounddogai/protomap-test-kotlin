package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 7828.0, in order. */
fun regionalFuelSurchargeRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7828.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun regionalFuelSurchargeRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun regionalFuelSurchargeRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalFuelSurchargeRecord(): Int {
    val policy = defaultPolicy("regional-fuel-surcharge-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
