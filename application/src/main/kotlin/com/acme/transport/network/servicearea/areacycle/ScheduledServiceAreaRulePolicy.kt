package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 84338.0, in order. */
fun scheduledServiceAreaRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84338.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledServiceAreaRule(): Int {
    val policy = defaultPolicy("scheduled-service-area-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledServiceAreaRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled service area rule score falls in. */
fun classifyScheduledServiceAreaRule(score: Double): String {
    if (score < 21.4) {
        return "medium"
    }
    if (score < 55.1) {
        return "low"
    }
    return "minimal"
}
