package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeDwellClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a dwell claim score falls in. */
fun classifyDwellClaim(score: Double): String {
    if (score < 15.5) {
        return "watch"
    }
    if (score < 17.2) {
        return "high"
    }
    if (score < 67.1) {
        return "critical"
    }
    if (score < 75.7) {
        return "ok"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellClaim(): Int {
    val policy = defaultPolicy("dwell-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun dwellClaimWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
