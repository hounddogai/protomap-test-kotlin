package com.acme.transport.network.servicearea.manifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a service area checkpoint score falls in. */
fun classifyServiceAreaCheckpoint(score: Double): String {
    if (score < 20.5) {
        return "watch"
    }
    if (score < 30.4) {
        return "critical"
    }
    if (score < 90.8) {
        return "minimal"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaCheckpoint(): Int {
    val policy = defaultPolicy("service-area-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeServiceAreaCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun serviceAreaCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
