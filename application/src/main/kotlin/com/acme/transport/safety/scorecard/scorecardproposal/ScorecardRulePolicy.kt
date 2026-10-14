package com.acme.transport.safety.scorecard.scorecardproposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardRule(): Int {
    val policy = defaultPolicy("scorecard-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 46229.0, in order. */
fun scorecardRuleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46229.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scorecardRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
