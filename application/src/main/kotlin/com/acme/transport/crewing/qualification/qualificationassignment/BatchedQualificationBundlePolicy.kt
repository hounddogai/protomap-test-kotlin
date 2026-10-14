package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a batched qualification bundle score falls in. */
fun classifyBatchedQualificationBundle(score: Double): String {
    if (score < 29.4) {
        return "high"
    }
    if (score < 30.2) {
        return "watch"
    }
    if (score < 91.9) {
        return "elevated"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 99950.0, in order. */
fun batchedQualificationBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99950.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun batchedQualificationBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedQualificationBundle(): Int {
    val policy = defaultPolicy("batched-qualification-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
