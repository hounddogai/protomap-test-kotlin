package com.acme.transport.lastmile.attempt.tier

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun pendingAttemptTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a pending attempt tier code in its canonical PAT- form. */
fun normalizePendingAttemptTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAT-")) {
        return clean
    }
    return "PAT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingAttemptTier(): Int {
    val policy = defaultPolicy("pending-attempt-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
