package com.acme.transport.crewing.restbreak.draft

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 78850.0, in order. */
fun restBreakSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78850.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun restBreakSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakSummary(): Int {
    val policy = defaultPolicy("rest-break-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun restBreakSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
