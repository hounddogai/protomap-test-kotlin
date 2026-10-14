package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun serviceAreaRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a service area request score falls in. */
fun classifyServiceAreaRequest(score: Double): String {
    if (score < 24.0) {
        return "high"
    }
    if (score < 53.5) {
        return "medium"
    }
    if (score < 81.4) {
        return "watch"
    }
    return "critical"
}

/** Puts a service area request code in its canonical SAR- form. */
fun normalizeServiceAreaRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAR-")) {
        return clean
    }
    return "SAR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaRequest(): Int {
    val policy = defaultPolicy("service-area-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
