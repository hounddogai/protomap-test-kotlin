package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeHoldAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldAdjustment(): Int {
    val policy = defaultPolicy("hold-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun holdAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a hold adjustment score falls in. */
fun classifyHoldAdjustment(score: Double): String {
    if (score < 7.4) {
        return "elevated"
    }
    if (score < 27.0) {
        return "critical"
    }
    if (score < 54.5) {
        return "watch"
    }
    if (score < 94.4) {
        return "low"
    }
    return "medium"
}
