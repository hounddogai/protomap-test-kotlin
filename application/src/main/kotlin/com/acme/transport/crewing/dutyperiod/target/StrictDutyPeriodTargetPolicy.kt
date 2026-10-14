package com.acme.transport.crewing.dutyperiod.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 85511.0, in order. */
fun strictDutyPeriodTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85511.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun strictDutyPeriodTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictDutyPeriodTarget(): Int {
    val policy = defaultPolicy("strict-duty-period-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict duty period target score falls in. */
fun classifyStrictDutyPeriodTarget(score: Double): String {
    if (score < 25.4) {
        return "minimal"
    }
    if (score < 94.1) {
        return "critical"
    }
    return "watch"
}
