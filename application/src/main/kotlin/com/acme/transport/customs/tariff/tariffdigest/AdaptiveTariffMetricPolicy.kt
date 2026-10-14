package com.acme.transport.customs.tariff.tariffdigest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTariffMetric(): Int {
    val policy = defaultPolicy("adaptive-tariff-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveTariffMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive tariff metric score falls in. */
fun classifyAdaptiveTariffMetric(score: Double): String {
    if (score < 29.3) {
        return "critical"
    }
    if (score < 53.6) {
        return "ok"
    }
    if (score < 58.1) {
        return "high"
    }
    return "minimal"
}
