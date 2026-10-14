package com.acme.transport.crewing.qualification.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun qualificationPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun qualificationPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 64869.0, in order. */
fun qualificationPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64869.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun qualificationPolicyBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
