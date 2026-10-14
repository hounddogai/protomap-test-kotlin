package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredEvChargeQuotaBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a tiered ev charge quota score falls in. */
fun classifyTieredEvChargeQuota(score: Double): String {
    if (score < 64.6) {
        return "elevated"
    }
    if (score < 65.9) {
        return "minimal"
    }
    return "low"
}

fun tieredEvChargeQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.89
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tiered ev charge quota code in its canonical TECQ- form. */
fun normalizeTieredEvChargeQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TECQ-")) {
        return clean
    }
    return "TECQ-${clean.replace(" ", "-")}"
}
