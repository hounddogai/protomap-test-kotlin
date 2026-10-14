package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedEmissionFactorPolicy(): Int {
    val policy = defaultPolicy("batched-emission-factor-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun batchedEmissionFactorPolicyBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a batched emission factor policy score falls in. */
fun classifyBatchedEmissionFactorPolicy(score: Double): String {
    if (score < 50.7) {
        return "elevated"
    }
    if (score < 94.0) {
        return "minimal"
    }
    return "high"
}

/** Puts a batched emission factor policy code in its canonical BEFP- form. */
fun normalizeBatchedEmissionFactorPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BEFP-")) {
        return clean
    }
    return "BEFP-${clean.replace(" ", "-")}"
}
