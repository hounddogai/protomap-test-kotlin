package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a pending near miss threshold code in its canonical PNMT- form. */
fun normalizePendingNearMissThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PNMT-")) {
        return clean
    }
    return "PNMT-${clean.replace(" ", "-")}"
}

fun pendingNearMissThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingNearMissThreshold(): Int {
    val policy = defaultPolicy("pending-near-miss-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
