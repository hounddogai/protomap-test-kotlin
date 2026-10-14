package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a fuel burn audit code in its canonical FBA- form. */
fun normalizeFuelBurnAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBA-")) {
        return clean
    }
    return "FBA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnAudit(): Int {
    val policy = defaultPolicy("fuel-burn-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a fuel burn audit score falls in. */
fun classifyFuelBurnAudit(score: Double): String {
    if (score < 26.5) {
        return "critical"
    }
    if (score < 40.4) {
        return "ok"
    }
    if (score < 41.8) {
        return "low"
    }
    return "watch"
}

fun fuelBurnAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
