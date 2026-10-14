package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun dutyPeriodQuotaBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodQuota(): Int {
    val policy = defaultPolicy("duty-period-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 14872.0, in order. */
fun dutyPeriodQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 14872.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dutyPeriodQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
