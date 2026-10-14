package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationBudget(): Int {
    val policy = defaultPolicy("declaration-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun declarationBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a declaration budget score falls in. */
fun classifyDeclarationBudget(score: Double): String {
    if (score < 57.6) {
        return "elevated"
    }
    if (score < 71.2) {
        return "ok"
    }
    if (score < 98.6) {
        return "medium"
    }
    return "watch"
}

fun declarationBudgetBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
