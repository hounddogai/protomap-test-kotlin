package com.acme.transport.network.zone.group

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun zoneManifestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneManifest(): Int {
    val policy = defaultPolicy("zone-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a zone manifest score falls in. */
fun classifyZoneManifest(score: Double): String {
    if (score < 3.2) {
        return "ok"
    }
    if (score < 61.8) {
        return "medium"
    }
    if (score < 86.8) {
        return "elevated"
    }
    return "minimal"
}
