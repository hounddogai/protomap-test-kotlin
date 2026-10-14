package com.acme.transport.network.hub.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a hub summary score falls in. */
fun classifyHubSummary(score: Double): String {
    if (score < 77.4) {
        return "ok"
    }
    if (score < 83.0) {
        return "elevated"
    }
    if (score < 86.1) {
        return "low"
    }
    return "watch"
}

fun hubSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHubSummary(): Int {
    val policy = defaultPolicy("hub-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a hub summary code in its canonical HS- form. */
fun normalizeHubSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HS-")) {
        return clean
    }
    return "HS-${clean.replace(" ", "-")}"
}
