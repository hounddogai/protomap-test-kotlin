package com.acme.transport.network.zone.profile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeLegacyZoneProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyZoneProfile(): Int {
    val policy = defaultPolicy("legacy-zone-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a legacy zone profile score falls in. */
fun classifyLegacyZoneProfile(score: Double): String {
    if (score < 8.4) {
        return "medium"
    }
    if (score < 21.8) {
        return "minimal"
    }
    if (score < 84.5) {
        return "high"
    }
    if (score < 86.2) {
        return "watch"
    }
    return "ok"
}

fun legacyZoneProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
