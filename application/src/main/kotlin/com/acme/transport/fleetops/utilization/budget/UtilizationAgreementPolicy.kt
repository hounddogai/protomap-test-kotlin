package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun utilizationAgreementBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a utilization agreement score falls in. */
fun classifyUtilizationAgreement(score: Double): String {
    if (score < 25.9) {
        return "critical"
    }
    if (score < 43.6) {
        return "watch"
    }
    return "low"
}

/** Puts a utilization agreement code in its canonical UA- form. */
fun normalizeUtilizationAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("UA-")) {
        return clean
    }
    return "UA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationAgreement(): Int {
    val policy = defaultPolicy("utilization-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
