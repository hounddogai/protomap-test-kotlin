package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualIncidentManifest(): Int {
    val policy = defaultPolicy("manual-incident-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual incident manifest score falls in. */
fun classifyManualIncidentManifest(score: Double): String {
    if (score < 1.9) {
        return "minimal"
    }
    if (score < 10.3) {
        return "medium"
    }
    if (score < 93.6) {
        return "elevated"
    }
    if (score < 95.7) {
        return "critical"
    }
    return "low"
}

fun manualIncidentManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeManualIncidentManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
