package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a attempt quota code in its canonical AQ- form. */
fun normalizeAttemptQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AQ-")) {
        return clean
    }
    return "AQ-${clean.replace(" ", "-")}"
}

fun attemptQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a attempt quota score falls in. */
fun classifyAttemptQuota(score: Double): String {
    if (score < 24.9) {
        return "elevated"
    }
    if (score < 27.2) {
        return "ok"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAttemptQuota(): Int {
    val policy = defaultPolicy("attempt-quota")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
