package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun seasonalServicePlanAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.49
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalServicePlanAgreement(): Int {
    val policy = defaultPolicy("seasonal-service-plan-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a seasonal service plan agreement code in its canonical SSPA- form. */
fun normalizeSeasonalServicePlanAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SSPA-")) {
        return clean
    }
    return "SSPA-${clean.replace(" ", "-")}"
}
