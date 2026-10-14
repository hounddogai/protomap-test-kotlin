package com.acme.transport.freightpay.dispute.disputeagreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveDisputeRollup(): Int {
    val policy = defaultPolicy("adaptive-dispute-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveDisputeRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a adaptive dispute rollup code in its canonical ADR- form. */
fun normalizeAdaptiveDisputeRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADR-")) {
        return clean
    }
    return "ADR-${clean.replace(" ", "-")}"
}
