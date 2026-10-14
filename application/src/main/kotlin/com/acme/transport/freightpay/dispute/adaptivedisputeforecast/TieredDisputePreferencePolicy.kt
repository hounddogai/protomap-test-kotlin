package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredDisputePreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tiered dispute preference score falls in. */
fun classifyTieredDisputePreference(score: Double): String {
    if (score < 7.3) {
        return "medium"
    }
    if (score < 30.1) {
        return "critical"
    }
    return "low"
}

fun tieredDisputePreferenceBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a tiered dispute preference code in its canonical TDP- form. */
fun normalizeTieredDisputePreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDP-")) {
        return clean
    }
    return "TDP-${clean.replace(" ", "-")}"
}
