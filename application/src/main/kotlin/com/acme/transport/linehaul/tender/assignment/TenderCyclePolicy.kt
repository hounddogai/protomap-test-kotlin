package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tenderCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a tender cycle score falls in. */
fun classifyTenderCycle(score: Double): String {
    if (score < 20.4) {
        return "critical"
    }
    if (score < 21.4) {
        return "ok"
    }
    if (score < 50.1) {
        return "elevated"
    }
    return "low"
}
