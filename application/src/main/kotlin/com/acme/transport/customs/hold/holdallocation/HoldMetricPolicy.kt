package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a hold metric score falls in. */
fun classifyHoldMetric(score: Double): String {
    if (score < 35.4) {
        return "minimal"
    }
    if (score < 40.3) {
        return "watch"
    }
    if (score < 47.1) {
        return "medium"
    }
    if (score < 60.8) {
        return "low"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldMetric(): Int {
    val policy = defaultPolicy("hold-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun holdMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a hold metric code in its canonical HM- form. */
fun normalizeHoldMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HM-")) {
        return clean
    }
    return "HM-${clean.replace(" ", "-")}"
}
