package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 88530.0, in order. */
fun dutyPeriodSegmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88530.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dutyPeriodSegmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a duty period segment score falls in. */
fun classifyDutyPeriodSegment(score: Double): String {
    if (score < 8.1) {
        return "ok"
    }
    if (score < 9.6) {
        return "elevated"
    }
    if (score < 28.2) {
        return "high"
    }
    if (score < 61.7) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDutyPeriodSegment(): Int {
    val policy = defaultPolicy("duty-period-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
