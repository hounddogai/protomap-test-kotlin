package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun weightedBrokerPlanWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a weighted broker plan code in its canonical WBP- form. */
fun normalizeWeightedBrokerPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WBP-")) {
        return clean
    }
    return "WBP-${clean.replace(" ", "-")}"
}

fun weightedBrokerPlanBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a weighted broker plan score falls in. */
fun classifyWeightedBrokerPlan(score: Double): String {
    if (score < 84.4) {
        return "ok"
    }
    if (score < 95.8) {
        return "elevated"
    }
    return "high"
}
