package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a manual service area claim code in its canonical MSAC- form. */
fun normalizeManualServiceAreaClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MSAC-")) {
        return clean
    }
    return "MSAC-${clean.replace(" ", "-")}"
}

fun manualServiceAreaClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.72
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualServiceAreaClaim(): Int {
    val policy = defaultPolicy("manual-service-area-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual service area claim score falls in. */
fun classifyManualServiceAreaClaim(score: Double): String {
    if (score < 5.1) {
        return "medium"
    }
    if (score < 81.0) {
        return "high"
    }
    return "low"
}
