package com.acme.transport.emissions.emissionfactor.request

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun batchedEmissionFactorBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedEmissionFactorBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a batched emission factor batch code in its canonical BEFB- form. */
fun normalizeBatchedEmissionFactorBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BEFB-")) {
        return clean
    }
    return "BEFB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedEmissionFactorBatch(): Int {
    val policy = defaultPolicy("batched-emission-factor-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
