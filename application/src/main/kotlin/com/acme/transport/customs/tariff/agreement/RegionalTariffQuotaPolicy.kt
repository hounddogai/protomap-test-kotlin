package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a regional tariff quota score falls in. */
fun classifyRegionalTariffQuota(score: Double): String {
    if (score < 10.5) {
        return "watch"
    }
    if (score < 80.8) {
        return "ok"
    }
    return "minimal"
}

fun regionalTariffQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
