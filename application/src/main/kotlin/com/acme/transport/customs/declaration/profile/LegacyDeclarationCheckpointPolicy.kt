package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 672.0, in order. */
fun legacyDeclarationCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 672.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a legacy declaration checkpoint score falls in. */
fun classifyLegacyDeclarationCheckpoint(score: Double): String {
    if (score < 2.0) {
        return "elevated"
    }
    if (score < 31.3) {
        return "critical"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyDeclarationCheckpoint(): Int {
    val policy = defaultPolicy("legacy-declaration-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyDeclarationCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
