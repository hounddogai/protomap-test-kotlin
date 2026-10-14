package com.acme.transport.freightpay.audit.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 8474.0, in order. */
fun rollingAuditMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8474.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingAuditMapping(): Int {
    val policy = defaultPolicy("rolling-audit-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingAuditMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
