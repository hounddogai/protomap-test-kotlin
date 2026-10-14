package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tieredLanePlanThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.16
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tieredLanePlanThresholdBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a tiered lane plan threshold code in its canonical TLPT- form. */
fun normalizeTieredLanePlanThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TLPT-")) {
        return clean
    }
    return "TLPT-${clean.replace(" ", "-")}"
}

/** Names the band a tiered lane plan threshold score falls in. */
fun classifyTieredLanePlanThreshold(score: Double): String {
    if (score < 16.6) {
        return "minimal"
    }
    if (score < 27.7) {
        return "watch"
    }
    if (score < 40.6) {
        return "low"
    }
    return "medium"
}
