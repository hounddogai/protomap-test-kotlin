package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryFuelBurnNotice(): Int {
    val policy = defaultPolicy("primary-fuel-burn-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a primary fuel burn notice code in its canonical PFBN- form. */
fun normalizePrimaryFuelBurnNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PFBN-")) {
        return clean
    }
    return "PFBN-${clean.replace(" ", "-")}"
}

fun primaryFuelBurnNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
