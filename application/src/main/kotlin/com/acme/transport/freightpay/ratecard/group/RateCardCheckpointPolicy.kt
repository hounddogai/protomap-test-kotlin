package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun rateCardCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardCheckpoint(): Int {
    val policy = defaultPolicy("rate-card-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rate card checkpoint code in its canonical RCC- form. */
fun normalizeRateCardCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCC-")) {
        return clean
    }
    return "RCC-${clean.replace(" ", "-")}"
}

/** Names the band a rate card checkpoint score falls in. */
fun classifyRateCardCheckpoint(score: Double): String {
    if (score < 72.1) {
        return "low"
    }
    if (score < 85.1) {
        return "minimal"
    }
    return "high"
}
