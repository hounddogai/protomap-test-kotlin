package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a fallback attempt milestone code in its canonical FAM- form. */
fun normalizeFallbackAttemptMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FAM-")) {
        return clean
    }
    return "FAM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackAttemptMilestone(): Int {
    val policy = defaultPolicy("fallback-attempt-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun fallbackAttemptMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 34040.0, in order. */
fun fallbackAttemptMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34040.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
