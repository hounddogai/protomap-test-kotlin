package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeStrictDockDoorTierLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictDockDoorTier(): Int {
    val policy = defaultPolicy("strict-dock-door-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a strict dock door tier score falls in. */
fun classifyStrictDockDoorTier(score: Double): String {
    if (score < 9.7) {
        return "critical"
    }
    if (score < 62.7) {
        return "high"
    }
    if (score < 63.1) {
        return "ok"
    }
    if (score < 87.5) {
        return "minimal"
    }
    return "medium"
}

fun strictDockDoorTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.62
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
