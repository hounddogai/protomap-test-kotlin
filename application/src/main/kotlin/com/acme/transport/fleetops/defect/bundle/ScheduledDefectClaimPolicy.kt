package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a scheduled defect claim score falls in. */
fun classifyScheduledDefectClaim(score: Double): String {
    if (score < 34.1) {
        return "ok"
    }
    if (score < 64.3) {
        return "low"
    }
    if (score < 80.9) {
        return "critical"
    }
    return "watch"
}

fun scheduledDefectClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.14
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledDefectClaim(): Int {
    val policy = defaultPolicy("scheduled-defect-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeScheduledDefectClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
