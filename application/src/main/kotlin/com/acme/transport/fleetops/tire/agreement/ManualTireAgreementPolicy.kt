package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun manualTireAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual tire agreement code in its canonical MTA- form. */
fun normalizeManualTireAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTA-")) {
        return clean
    }
    return "MTA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualTireAgreement(): Int {
    val policy = defaultPolicy("manual-tire-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
