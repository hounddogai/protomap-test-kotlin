package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun batchedAttemptOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedAttemptOverride(): Int {
    val policy = defaultPolicy("batched-attempt-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched attempt override score falls in. */
fun classifyBatchedAttemptOverride(score: Double): String {
    if (score < 32.0) {
        return "high"
    }
    if (score < 63.7) {
        return "critical"
    }
    return "minimal"
}

/** Puts a batched attempt override code in its canonical BAO- form. */
fun normalizeBatchedAttemptOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BAO-")) {
        return clean
    }
    return "BAO-${clean.replace(" ", "-")}"
}
