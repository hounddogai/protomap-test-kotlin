package com.acme.transport.network.servicearea.areamanifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a regional service area manifest score falls in. */
fun classifyRegionalServiceAreaManifest(score: Double): String {
    if (score < 53.3) {
        return "elevated"
    }
    if (score < 79.7) {
        return "high"
    }
    return "low"
}

fun regionalServiceAreaManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalServiceAreaManifest(): Int {
    val policy = defaultPolicy("regional-service-area-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalServiceAreaManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
